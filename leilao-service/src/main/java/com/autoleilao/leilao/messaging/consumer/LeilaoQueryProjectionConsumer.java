package com.autoleilao.leilao.messaging.consumer;

import com.autoleilao.leilao.command.model.LanceEmbedded;
import com.autoleilao.leilao.messaging.event.LoteLeilaoSyncEvent;
import com.autoleilao.leilao.model.Lance;
import com.autoleilao.leilao.model.LoteLeilao;
import com.autoleilao.leilao.repository.LoteLeilaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Consumidor CQRS do Leilão: projeta os eventos vindos do Command (CosmosDB)
 * nas tabelas relacionais 'lotes_leilao_query' e 'lances_query' no Azure SQL Server.
 */
@Component
public class LeilaoQueryProjectionConsumer {

    private static final Logger log = LoggerFactory.getLogger(LeilaoQueryProjectionConsumer.class);

    private final LoteLeilaoRepository queryRepository;

    public LeilaoQueryProjectionConsumer(LoteLeilaoRepository queryRepository) {
        this.queryRepository = queryRepository;
    }

    @Transactional
    public LoteLeilao processarEventoSincronizacao(LoteLeilaoSyncEvent event) {
        if (event == null || event.getId() == null) {
            return null;
        }

        log.info("[CQRS-QUERY PROJECTION -> Azure SQL] Atualizando projeção do Lote ID={} Evento='{}' LanceAtual={}",
                event.getId(), event.getTipoEvento(), event.getLanceAtual());

        LoteLeilao view = queryRepository.findById(event.getId()).orElseGet(LoteLeilao::new);
        view.setId(event.getId());
        view.setVeiculoId(event.getVeiculoId());
        view.setMarca(event.getMarca());
        view.setModelo(event.getModelo());
        view.setAno(event.getAno());
        view.setPlaca(event.getPlaca());
        view.setCor(event.getCor());
        view.setTipo(event.getTipo());
        view.setValorMinimo(event.getValorMinimo());
        view.setDescricao(event.getDescricao());
        view.setLanceAtual(event.getLanceAtual());
        view.setTotalLances(event.getTotalLances() != null ? event.getTotalLances() : 0);
        view.setTempoRestante(event.getTempoRestante());
        view.setStatus(event.getStatus() != null ? event.getStatus() : "ativo");
        view.setDataCriacao(event.getDataCriacao() != null ? event.getDataCriacao() : LocalDateTime.now());

        List<Lance> novosLances = new ArrayList<>();
        if (event.getBids() != null) {
            for (LanceEmbedded emb : event.getBids()) {
                Lance lance = new Lance(emb.getBidder(), emb.getValor(), view, emb.getTime());
                if (emb.getDataHora() != null) {
                    lance.setDataHora(emb.getDataHora());
                }
                novosLances.add(lance);
            }
        }
        view.getBids().clear();
        view.getBids().addAll(novosLances);

        return queryRepository.save(view);
    }
}
