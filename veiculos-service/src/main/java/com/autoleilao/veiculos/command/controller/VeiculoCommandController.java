package com.autoleilao.veiculos.command.controller;

import com.autoleilao.veiculos.command.service.VeiculoCommandService;
import com.autoleilao.veiculos.dto.VeiculoDTO;
import com.autoleilao.veiculos.model.Veiculo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/veiculos")
@Tag(name = "1. Veículos — COMMAND (Escrita / Azure Cosmos DB)",
     description = "Operações de mutação de estado (CQRS Command). Grava no Azure Cosmos DB (veiculos_command_db) e publica eventos no Azure Service Bus.")
public class VeiculoCommandController {

    private final VeiculoCommandService commandService;

    public VeiculoCommandController(VeiculoCommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping
    @Operation(summary = "[COMMAND] Cadastrar novo veículo",
               description = "Grava documento no Azure Cosmos DB (veiculos_command_db), sincroniza projeção SQL via Azure Service Bus e cria lote automático no leilao-service")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Veículo gravado no CosmosDB e evento publicado no Azure Service Bus"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou placa duplicada")
    })
    public ResponseEntity<Veiculo> cadastrar(@Valid @RequestBody VeiculoDTO dto) {
        Veiculo criado = commandService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "[COMMAND] Atualizar veículo existente",
               description = "Atualiza o documento no Azure Cosmos DB e dispara evento de sincronização para o Azure SQL Server")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Veículo atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    })
    public ResponseEntity<Veiculo> atualizar(@PathVariable("id") Long id, @Valid @RequestBody VeiculoDTO dto) {
        return ResponseEntity.ok(commandService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "[COMMAND] Excluir veículo do catálogo",
               description = "Remove do Azure Cosmos DB e publica evento de remoção para o Read Model no Azure SQL Server")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Veículo excluído com sucesso"),
        @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    })
    public ResponseEntity<Void> excluir(@PathVariable("id") Long id) {
        commandService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
