package com.autoleilao.leilao.service;

import com.autoleilao.leilao.dto.CriarLoteDTO;
import com.autoleilao.leilao.dto.NovoLanceDTO;
import com.autoleilao.leilao.exception.RecursoNaoEncontradoException;
import com.autoleilao.leilao.exception.RegraNegocioException;
import com.autoleilao.leilao.model.Lance;
import com.autoleilao.leilao.model.LoteLeilao;
import com.autoleilao.leilao.repository.LoteLeilaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class LeilaoService {

    public static final BigDecimal INCREMENTO_MINIMO = new BigDecimal("500.00");

    private final LoteLeilaoRepository loteRepository;

    public LeilaoService(LoteLeilaoRepository loteRepository) {
        this.loteRepository = loteRepository;
    }

    @Transactional(readOnly = true)
    public List<LoteLeilao> listarLotes(String status) {
        if (status != null && !status.isBlank()) {
            return loteRepository.findByStatusIgnoreCase(status.trim());
        }
        return loteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public LoteLeilao buscarPorId(Long id) {
        return loteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lote de leilão com ID " + id + " não encontrado."));
    }

    @Transactional
    public LoteLeilao darLance(Long loteId, NovoLanceDTO dto) {
        LoteLeilao lote = buscarPorId(loteId);

        if (!"ativo".equalsIgnoreCase(lote.getStatus())) {
            throw new RegraNegocioException("Não é possível enviar lances para um lote já encerrado.");
        }

        BigDecimal valorMinimoExigido = lote.getLanceAtual().add(INCREMENTO_MINIMO);
        if (dto.getValor().compareTo(valorMinimoExigido) < 0) {
            throw new RegraNegocioException(String.format(
                    "O lance deve ser de no mínimo R$ %.2f (lance atual R$ %.2f + incremento mínimo de R$ %.2f).",
                    valorMinimoExigido, lote.getLanceAtual(), INCREMENTO_MINIMO
            ));
        }

        Lance novoLance = new Lance(dto.getBidder().trim(), dto.getValor(), lote);
        lote.addLance(novoLance);

        // Regra Anti-Sniping: Se faltar menos de 60 segundos, prorroga o cronômetro para 120s
        if (lote.getTempoRestante() != null && lote.getTempoRestante() < 60) {
            lote.setTempoRestante(120L);
        }

        return loteRepository.save(lote);
    }

    @Transactional
    public LoteLeilao criarLote(CriarLoteDTO dto) {
        LoteLeilao lote = new LoteLeilao(
                dto.getVeiculoId(),
                dto.getMarca(),
                dto.getModelo(),
                dto.getAno(),
                dto.getPlaca(),
                dto.getCor(),
                dto.getTipo(),
                dto.getValorMinimo(),
                dto.getDescricao(),
                dto.getTempoRestante() != null ? dto.getTempoRestante() : 3600L
        );
        return loteRepository.save(lote);
    }

    @Transactional
    public LoteLeilao encerrarLeilao(Long id) {
        LoteLeilao lote = buscarPorId(id);
        lote.setStatus("encerrado");
        lote.setTempoRestante(0L);
        return loteRepository.save(lote);
    }
}
