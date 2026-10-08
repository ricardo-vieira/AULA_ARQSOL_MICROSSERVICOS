package com.autoleilao.leilao.controller;

import com.autoleilao.leilao.dto.CriarLoteDTO;
import com.autoleilao.leilao.dto.NovoLanceDTO;
import com.autoleilao.leilao.model.LoteLeilao;
import com.autoleilao.leilao.service.LeilaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leiloes")
@Tag(name = "Leilões e Lances", description = "Operações do pregão ao vivo, submissão de lances e gestão de lotes")
@CrossOrigin(origins = "*")
public class LeilaoController {

    private final LeilaoService service;

    public LeilaoController(LeilaoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar lotes de leilão", description = "Retorna todos os lotes de veículos disponíveis para lance")
    public ResponseEntity<List<LoteLeilao>> listar(@RequestParam(name = "status", required = false) String status) {
        return ResponseEntity.ok(service.listarLotes(status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar lote por ID com histórico de lances")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lote encontrado"),
        @ApiResponse(responseCode = "404", description = "Lote não encontrado")
    })
    public ResponseEntity<LoteLeilao> buscarPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping("/{id}/lances")
    @Operation(summary = "Dar lance no lote", description = "Registra um novo lance. O valor deve ser maior ou igual ao lance atual + R$ 500,00.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Lance computado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Lance menor que o mínimo exigido ou leilão encerrado"),
        @ApiResponse(responseCode = "404", description = "Lote não encontrado")
    })
    public ResponseEntity<LoteLeilao> darLance(@PathVariable("id") Long id, @Valid @RequestBody NovoLanceDTO dto) {
        LoteLeilao atualizado = service.darLance(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(atualizado);
    }

    @PostMapping
    @Operation(summary = "Criar novo lote de leilão")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Lote criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<LoteLeilao> criarLote(@Valid @RequestBody CriarLoteDTO dto) {
        LoteLeilao criado = service.criarLote(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PatchMapping("/{id}/encerrar")
    @Operation(summary = "Encerrar lote de leilão")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lote encerrado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Lote não encontrado")
    })
    public ResponseEntity<LoteLeilao> encerrar(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.encerrarLeilao(id));
    }
}
