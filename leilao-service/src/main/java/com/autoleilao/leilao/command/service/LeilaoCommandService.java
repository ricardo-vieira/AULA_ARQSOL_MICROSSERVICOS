package com.autoleilao.leilao.command.service;

import com.autoleilao.leilao.command.model.LanceEmbedded;
import com.autoleilao.leilao.command.model.LoteLeilaoDocument;
import com.autoleilao.leilao.command.repository.LoteLeilaoCommandRepository;
import com.autoleilao.leilao.dto.CriarLoteDTO;
import com.autoleilao.leilao.dto.NovoLanceDTO;
import com.autoleilao.leilao.exception.RecursoNaoEncontradoException;
import com.autoleilao.leilao.exception.RegraNegocioException;
import com.autoleilao.leilao.messaging.event.LoteLeilaoSyncEvent;
import com.autoleilao.leilao.messaging.event.VeiculoCriadoIntegrationEvent;
import com.autoleilao.leilao.messaging.publisher.LeilaoServiceBusGateway;
import com.autoleilao.leilao.model.LoteLeilao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class LeilaoCommandService {

    private static final Logger log = LoggerFactory.getLogger(LeilaoCommandService.class);
    public static final BigDecimal INCREMENTO_MINIMO = new BigDecimal("500.00");

    private final LoteLeilaoCommandRepository commandRepository;
    private final LeilaoServiceBusGateway serviceBusGateway;

    public LeilaoCommandService(LoteLeilaoCommandRepository commandRepository,
                                @Lazy LeilaoServiceBusGateway serviceBusGateway) {
        this.commandRepository = commandRepository;
        this.serviceBusGateway = serviceBusGateway;
    }

    public LoteLeilao darLance(Long loteId, NovoLanceDTO dto) {
        return darLanceComHorario(loteId, dto, null);
    }

    public LoteLeilao darLanceComHorario(Long loteId, NovoLanceDTO dto, String customTime) {
        LoteLeilaoDocument doc = commandRepository.findById(loteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lote de leilão com ID " + loteId + " não encontrado."));

        if (!"ativo".equalsIgnoreCase(doc.getStatus())) {
            throw new RegraNegocioException("Não é possível enviar lances para um lote já encerrado.");
        }

        BigDecimal minimoExigido = doc.getLanceAtual().add(INCREMENTO_MINIMO);
        if (dto.getValor().compareTo(minimoExigido) < 0) {
            throw new RegraNegocioException(String.format(
                    "O lance deve ser de no mínimo R$ %.2f (lance atual R$ %.2f + incremento mínimo de R$ %.2f).",
                    minimoExigido, doc.getLanceAtual(), INCREMENTO_MINIMO
            ));
        }

        doc.addLance(new LanceEmbedded(commandRepository.nextLanceId(), dto.getBidder().trim(), dto.getValor(), customTime));

        if (doc.getTempoRestante() != null && doc.getTempoRestante() < 60) {
            doc.setTempoRestante(120L);
        }

        LoteLeilaoDocument salvo = commandRepository.save(doc);
        return serviceBusGateway.publicarEventoLote(toSyncEvent("LANCE_REGISTRADO", salvo));
    }

    public LoteLeilao criarLote(CriarLoteDTO dto) {
        LoteLeilaoDocument doc = new LoteLeilaoDocument(
                null, dto.getVeiculoId(), dto.getMarca(), dto.getModelo(), dto.getAno(),
                dto.getPlaca(), dto.getCor(), dto.getTipo(), dto.getValorMinimo(),
                dto.getDescricao(), dto.getTempoRestante() != null ? dto.getTempoRestante() : 3600L
        );
        LoteLeilaoDocument salvo = commandRepository.save(doc);
        return serviceBusGateway.publicarEventoLote(toSyncEvent("LOTE_CRIADO", salvo));
    }

    public LoteLeilao encerrarLeilao(Long id) {
        LoteLeilaoDocument doc = commandRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lote de leilão com ID " + id + " não encontrado."));
        doc.setStatus("encerrado");
        doc.setTempoRestante(0L);
        LoteLeilaoDocument salvo = commandRepository.save(doc);
        return serviceBusGateway.publicarEventoLote(toSyncEvent("LOTE_ENCERRADO", salvo));
    }

    public void criarLoteAutomaticoPorEventoVeiculo(VeiculoCriadoIntegrationEvent event) {
        if (event == null || event.getPlaca() == null || commandRepository.existsByPlacaIgnoreCase(event.getPlaca())) {
            return;
        }
        CriarLoteDTO dto = new CriarLoteDTO();
        dto.setVeiculoId(event.getId());
        dto.setMarca(event.getMarca());
        dto.setModelo(event.getModelo());
        dto.setAno(event.getAno());
        dto.setPlaca(event.getPlaca());
        dto.setCor(event.getCor());
        dto.setTipo(event.getTipo());
        dto.setValorMinimo(event.getValorMinimo());
        dto.setDescricao(event.getDescricao());
        dto.setTempoRestante(3600L);

        LoteLeilao criado = criarLote(dto);
        log.info("[INTEGRAÇÃO VEÍCULOS -> LEILÃO] Lote ID={} criado automaticamente para placa {}", criado.getId(), event.getPlaca());
    }

    private LoteLeilaoSyncEvent toSyncEvent(String tipoEvento, LoteLeilaoDocument doc) {
        LoteLeilaoSyncEvent ev = new LoteLeilaoSyncEvent();
        ev.setTipoEvento(tipoEvento);
        ev.setId(doc.getId());
        ev.setVeiculoId(doc.getVeiculoId());
        ev.setMarca(doc.getMarca());
        ev.setModelo(doc.getModelo());
        ev.setAno(doc.getAno());
        ev.setPlaca(doc.getPlaca());
        ev.setCor(doc.getCor());
        ev.setTipo(doc.getTipo());
        ev.setValorMinimo(doc.getValorMinimo());
        ev.setDescricao(doc.getDescricao());
        ev.setLanceAtual(doc.getLanceAtual());
        ev.setTotalLances(doc.getTotalLances());
        ev.setTempoRestante(doc.getTempoRestante());
        ev.setStatus(doc.getStatus());
        ev.setDataCriacao(doc.getDataCriacao());
        ev.setBids(doc.getBids());
        return ev;
    }
}
