package com.autoleilao.leilao.command.controller;

import com.autoleilao.leilao.command.service.LeilaoCommandService;
import com.autoleilao.leilao.dto.CriarLoteDTO;
import com.autoleilao.leilao.dto.NovoLanceDTO;
import com.autoleilao.leilao.messaging.event.VeiculoCriadoIntegrationEvent;
import com.autoleilao.leilao.model.LoteLeilao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leiloes")
@Tag(name = "1. Leilão e Lances — COMMAND (Escrita / Azure Cosmos DB)",
     description = "Operações de alta velocidade de escrita (CQRS Command). Grava o Aggregate Root (Lote + Lances) no Azure Cosmos DB (leilao_command_db) com @Version Optimistic Locking e publica eventos no Azure Service Bus.")
@CrossOrigin(origins = "*")
public class LeilaoCommandController {

    private final LeilaoCommandService commandService;

    public LeilaoCommandController(LeilaoCommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping("/{id}/lances")
    @Operation(summary = "[COMMAND] Dar lance no lote (Baixíssima Latência / CosmosDB Aggregate)",
               description = "Registra o lance atomicamente no documento do Lote no Azure Cosmos DB (valor >= lanceAtual + R$ 500,00, regra anti-sniping +60s) e sincroniza com o Azure SQL Server via Service Bus.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Lance gravado no CosmosDB e publicado no Azure Service Bus"),
        @ApiResponse(responseCode = "400", description = "Lance menor que o mínimo exigido ou leilão encerrado"),
        @ApiResponse(responseCode = "404", description = "Lote não encontrado")
    })
    public ResponseEntity<LoteLeilao> darLance(@PathVariable("id") Long id, @Valid @RequestBody NovoLanceDTO dto) {
        LoteLeilao atualizado = commandService.darLance(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(atualizado);
    }

    @PostMapping
    @Operation(summary = "[COMMAND] Criar novo lote de leilão no CosmosDB")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Lote criado no CosmosDB e sincronizado via Service Bus"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<LoteLeilao> criarLote(@Valid @RequestBody CriarLoteDTO dto) {
        LoteLeilao criado = commandService.criarLote(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PatchMapping("/{id}/encerrar")
    @Operation(summary = "[COMMAND] Encerrar lote de leilão")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lote encerrado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Lote não encontrado")
    })
    public ResponseEntity<LoteLeilao> encerrar(@PathVariable("id") Long id) {
        return ResponseEntity.ok(commandService.encerrarLeilao(id));
    }

    @PostMapping("/internal/eventos/veiculo-criado")
    @Operation(summary = "[EVENTO INTEGRAÇÃO] Receber evento de novo veículo cadastrado e criar lote automaticamente")
    public ResponseEntity<Void> receberEventoVeiculoCriado(@RequestBody VeiculoCriadoIntegrationEvent event) {
        commandService.criarLoteAutomaticoPorEventoVeiculo(event);
        return ResponseEntity.accepted().build();
    }
}
