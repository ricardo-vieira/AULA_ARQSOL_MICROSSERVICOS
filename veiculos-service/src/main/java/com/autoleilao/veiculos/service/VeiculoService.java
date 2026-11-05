package com.autoleilao.veiculos.service;

import com.autoleilao.veiculos.command.service.VeiculoCommandService;
import com.autoleilao.veiculos.dto.EstatisticasDTO;
import com.autoleilao.veiculos.dto.VeiculoDTO;
import com.autoleilao.veiculos.model.Veiculo;
import com.autoleilao.veiculos.query.service.VeiculoQueryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Fachada unificada que delega operações de Escrita para VeiculoCommandService (CosmosDB)
 * e operações de Leitura para VeiculoQueryService (Azure SQL Server).
 */
@Service
public class VeiculoService {

    private final VeiculoCommandService commandService;
    private final VeiculoQueryService queryService;

    public VeiculoService(VeiculoCommandService commandService, VeiculoQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    public List<Veiculo> listarTodos(String tipo, String marca) {
        return queryService.listarTodos(tipo, marca);
    }

    public Veiculo buscarPorId(Long id) {
        return queryService.buscarPorId(id);
    }

    public Veiculo cadastrar(VeiculoDTO dto) {
        return commandService.cadastrar(dto);
    }

    public Veiculo atualizar(Long id, VeiculoDTO dto) {
        return commandService.atualizar(id, dto);
    }

    public void excluir(Long id) {
        commandService.excluir(id);
    }

    public EstatisticasDTO obterEstatisticas() {
        return queryService.obterEstatisticas();
    }
}

