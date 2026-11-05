package com.autoleilao.leilao.service;

import com.autoleilao.leilao.command.service.LeilaoCommandService;
import com.autoleilao.leilao.dto.CriarLoteDTO;
import com.autoleilao.leilao.dto.NovoLanceDTO;
import com.autoleilao.leilao.model.LoteLeilao;
import com.autoleilao.leilao.query.service.LeilaoQueryService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Fachada CQRS que delega mutações de estado (lances, abertura e encerramento de lotes)
 * para LeilaoCommandService (CosmosDB) e consultas para LeilaoQueryService (Azure SQL Server).
 */
@Service
public class LeilaoService {

    public static final BigDecimal INCREMENTO_MINIMO = LeilaoCommandService.INCREMENTO_MINIMO;

    private final LeilaoCommandService commandService;
    private final LeilaoQueryService queryService;

    public LeilaoService(LeilaoCommandService commandService, LeilaoQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    public List<LoteLeilao> listarLotes(String status) {
        return queryService.listarLotes(status);
    }

    public LoteLeilao buscarPorId(Long id) {
        return queryService.buscarPorId(id);
    }

    public LoteLeilao darLance(Long loteId, NovoLanceDTO dto) {
        return commandService.darLance(loteId, dto);
    }

    public LoteLeilao criarLote(CriarLoteDTO dto) {
        return commandService.criarLote(dto);
    }

    public LoteLeilao encerrarLeilao(Long id) {
        return commandService.encerrarLeilao(id);
    }
}

