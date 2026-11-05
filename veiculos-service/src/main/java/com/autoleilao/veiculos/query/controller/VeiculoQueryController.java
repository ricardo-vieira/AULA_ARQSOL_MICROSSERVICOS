package com.autoleilao.veiculos.query.controller;

import com.autoleilao.veiculos.command.repository.VeiculoCommandRepository;
import com.autoleilao.veiculos.dto.EstatisticasDTO;
import com.autoleilao.veiculos.messaging.publisher.VeiculoServiceBusGateway;
import com.autoleilao.veiculos.model.Veiculo;
import com.autoleilao.veiculos.query.service.VeiculoQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/veiculos")
@Tag(name = "2. Veículos — QUERY (Leitura / Azure SQL Server)",
     description = "Consultas otimizadas e agregações relacionais (CQRS Query). Lê exclusivamente do Azure SQL Server (tabela veiculos_query).")
@CrossOrigin(origins = "*")
public class VeiculoQueryController {

    private final VeiculoQueryService queryService;
    private final VeiculoCommandRepository commandRepository;
    private final VeiculoServiceBusGateway serviceBusGateway;

    public VeiculoQueryController(VeiculoQueryService queryService,
                                  VeiculoCommandRepository commandRepository,
                                  VeiculoServiceBusGateway serviceBusGateway) {
        this.queryService = queryService;
        this.commandRepository = commandRepository;
        this.serviceBusGateway = serviceBusGateway;
    }

    @GetMapping
    @Operation(summary = "[QUERY] Listar veículos",
               description = "Consulta o Read Model no Azure SQL Server (tabela veiculos_query), suportando filtros por tipo ou marca")
    public ResponseEntity<List<Veiculo>> listar(
            @RequestParam(name = "tipo", required = false) String tipo,
            @RequestParam(name = "marca", required = false) String marca) {
        return ResponseEntity.ok(queryService.listarTodos(tipo, marca));
    }

    @GetMapping("/{id}")
    @Operation(summary = "[QUERY] Buscar veículo por ID no Read Model (Azure SQL Server)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Veículo encontrado no Read Model"),
        @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    })
    public ResponseEntity<Veiculo> buscarPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(queryService.buscarPorId(id));
    }

    @GetMapping("/estatisticas")
    @Operation(summary = "[QUERY] Estatísticas relacionais do catálogo (Azure SQL Server)")
    public ResponseEntity<EstatisticasDTO> estatisticas() {
        return ResponseEntity.ok(queryService.obterEstatisticas());
    }

    @GetMapping("/cqrs-status")
    @Operation(summary = "[DIAGNÓSTICO CQRS] Inspecionar estado do Command DB (CosmosDB), Service Bus e Query DB (Azure SQL)")
    public ResponseEntity<Map<String, Object>> cqrsStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("servico", "veiculos-service");
        status.put("padraoArquitetural", "CQRS + Database per Service");
        status.put("commandDatabaseName", commandRepository.getDatabaseName());
        status.put("commandCloudCosmosAtivo", commandRepository.isCloudCosmosActive());
        status.put("totalDocumentosCommand", commandRepository.count());
        status.put("queryTableName", "veiculos_query (Azure SQL Server)");
        status.put("totalRegistrosQuery", queryService.countReadModel());
        status.put("serviceBusCloudAtivo", serviceBusGateway.isCloudServiceBusActive());
        status.put("filaSincronizacaoCqrs", serviceBusGateway.getCqrsQueueName());
        status.put("filaIntegracaoLeilao", serviceBusGateway.getIntegrationQueueName());
        return ResponseEntity.ok(status);
    }
}
