package com.autoleilao.leilao.query.controller;

import com.autoleilao.leilao.command.repository.LoteLeilaoCommandRepository;
import com.autoleilao.leilao.messaging.publisher.LeilaoServiceBusGateway;
import com.autoleilao.leilao.model.LoteLeilao;
import com.autoleilao.leilao.query.service.LeilaoQueryService;
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
@RequestMapping("/api/leiloes")
@Tag(name = "2. Leilão e Lances — QUERY (Leitura / Azure SQL Server)",
     description = "Consultas estruturadas para os Microfrontends (CQRS Query). Lê exclusivamente do Azure SQL Server (tabelas lotes_leilao_query e lances_query), isolando a carga de leitura da disputa de lances.")
@CrossOrigin(origins = "*")
public class LeilaoQueryController {

    private final LeilaoQueryService queryService;
    private final LoteLeilaoCommandRepository commandRepository;
    private final LeilaoServiceBusGateway serviceBusGateway;

    public LeilaoQueryController(LeilaoQueryService queryService,
                                 LoteLeilaoCommandRepository commandRepository,
                                 LeilaoServiceBusGateway serviceBusGateway) {
        this.queryService = queryService;
        this.commandRepository = commandRepository;
        this.serviceBusGateway = serviceBusGateway;
    }

    @GetMapping
    @Operation(summary = "[QUERY] Listar lotes de leilão no Read Model (Azure SQL Server)",
               description = "Retorna todos os lotes de veículos disponíveis para lance consultando o Azure SQL Server")
    public ResponseEntity<List<LoteLeilao>> listar(@RequestParam(name = "status", required = false) String status) {
        return ResponseEntity.ok(queryService.listarLotes(status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "[QUERY] Buscar lote por ID com histórico cronológico de lances no Read Model (Azure SQL Server)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lote encontrado no Read Model"),
        @ApiResponse(responseCode = "404", description = "Lote não encontrado")
    })
    public ResponseEntity<LoteLeilao> buscarPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(queryService.buscarPorId(id));
    }

    @GetMapping("/cqrs-status")
    @Operation(summary = "[DIAGNÓSTICO CQRS] Inspecionar estado do Command DB (CosmosDB), Service Bus e Query DB (Azure SQL)")
    public ResponseEntity<Map<String, Object>> cqrsStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("servico", "leilao-service");
        status.put("padraoArquitetural", "CQRS + Database per Service + Aggregate Root (@Version)");
        status.put("commandDatabaseName", commandRepository.getDatabaseName());
        status.put("commandCloudCosmosAtivo", commandRepository.isCloudCosmosActive());
        status.put("totalAgregadosCommand", commandRepository.count());
        status.put("queryTables", "lotes_leilao_query + lances_query (Azure SQL Server)");
        status.put("totalLotesQuery", queryService.countReadModel());
        status.put("serviceBusCloudAtivo", serviceBusGateway.isCloudServiceBusActive());
        status.put("filaSincronizacaoCqrs", serviceBusGateway.getCqrsQueueName());
        status.put("filaIntegracaoVeiculos", serviceBusGateway.getIntegrationQueueName());
        return ResponseEntity.ok(status);
    }
}
