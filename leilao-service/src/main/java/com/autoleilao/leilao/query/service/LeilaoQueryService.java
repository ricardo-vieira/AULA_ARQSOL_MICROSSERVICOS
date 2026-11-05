package com.autoleilao.leilao.query.service;

import com.autoleilao.leilao.exception.RecursoNaoEncontradoException;
import com.autoleilao.leilao.model.LoteLeilao;
import com.autoleilao.leilao.repository.LoteLeilaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço de Leitura (QUERY) do padrão CQRS para Leilões e Lances.
 * Lê exclusivamente do banco relacional Azure SQL Server (tabelas lotes_leilao_query e lances_query).
 */
@Service
public class LeilaoQueryService {

    private final LoteLeilaoRepository queryRepository;

    public LeilaoQueryService(LoteLeilaoRepository queryRepository) {
        this.queryRepository = queryRepository;
    }

    @Transactional(readOnly = true)
    public List<LoteLeilao> listarLotes(String status) {
        if (status != null && !status.isBlank()) {
            return queryRepository.findByStatusIgnoreCase(status.trim());
        }
        return queryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public LoteLeilao buscarPorId(Long id) {
        return queryRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lote de leilão com ID " + id + " não encontrado no Read Model (SQL Server)."));
    }

    @Transactional(readOnly = true)
    public long countReadModel() {
        return queryRepository.count();
    }
}
