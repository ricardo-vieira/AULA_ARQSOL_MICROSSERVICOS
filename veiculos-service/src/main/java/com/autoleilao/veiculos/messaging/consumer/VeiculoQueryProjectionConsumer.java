package com.autoleilao.veiculos.messaging.consumer;

import com.autoleilao.veiculos.messaging.event.VeiculoSyncEvent;
import com.autoleilao.veiculos.model.Veiculo;
import com.autoleilao.veiculos.repository.VeiculoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Consumidor CQRS responsável por projetar eventos do Command (CosmosDB)
 * na base de leitura relacional (Azure SQL Server - tabela veiculos_query).
 */
@Component
public class VeiculoQueryProjectionConsumer {

    private static final Logger log = LoggerFactory.getLogger(VeiculoQueryProjectionConsumer.class);

    private final VeiculoRepository queryRepository;

    public VeiculoQueryProjectionConsumer(VeiculoRepository queryRepository) {
        this.queryRepository = queryRepository;
    }

    @Transactional
    public void processarEventoSincronizacao(VeiculoSyncEvent event) {
        if (event == null || event.getId() == null) {
            return;
        }

        log.info("[CQRS-QUERY PROJECTION -> Azure SQL] Processando evento '{}' para Veículo ID={} Placa={}",
                event.getTipoEvento(), event.getId(), event.getPlaca());

        if ("EXCLUIDO".equalsIgnoreCase(event.getTipoEvento())) {
            queryRepository.findById(event.getId()).ifPresent(queryRepository::delete);
            return;
        }

        Veiculo view = queryRepository.findById(event.getId()).orElseGet(Veiculo::new);
        view.setId(event.getId());
        view.setMarca(event.getMarca());
        view.setModelo(event.getModelo());
        view.setAno(event.getAno());
        view.setPlaca(event.getPlaca());
        view.setCor(event.getCor());
        view.setTipo(event.getTipo());
        view.setValorMinimo(event.getValorMinimo());
        view.setDescricao(event.getDescricao());
        view.setStatus(event.getStatus() != null ? event.getStatus() : "DISPONIVEL");
        view.setDataCadastro(event.getDataCadastro() != null ? event.getDataCadastro() : LocalDateTime.now());

        queryRepository.save(view);
    }
}
