package com.autoleilao.veiculos.controller;

import com.autoleilao.veiculos.dto.EstatisticasDTO;
import com.autoleilao.veiculos.dto.VeiculoDTO;
import com.autoleilao.veiculos.model.Veiculo;
import com.autoleilao.veiculos.service.VeiculoService;
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
@RequestMapping("/api/veiculos")
@Tag(name = "Veículos", description = "Operações de CRUD e consulta do catálogo de veículos")
@CrossOrigin(origins = "*")
public class VeiculoController {

    private final VeiculoService service;

    public VeiculoController(VeiculoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar veículos", description = "Retorna todos os veículos cadastrados, com opção de filtro por tipo ou marca")
    public ResponseEntity<List<Veiculo>> listar(
            @RequestParam(name = "tipo", required = false) String tipo,
            @RequestParam(name = "marca", required = false) String marca) {
        return ResponseEntity.ok(service.listarTodos(tipo, marca));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar veículo por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Veículo encontrado"),
        @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    })
    public ResponseEntity<Veiculo> buscarPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo veículo", description = "Valida os dados e cria um novo veículo no catálogo")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Veículo cadastrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou placa duplicada")
    })
    public ResponseEntity<Veiculo> cadastrar(@Valid @RequestBody VeiculoDTO dto) {
        Veiculo criado = service.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar veículo existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Veículo atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    })
    public ResponseEntity<Veiculo> atualizar(@PathVariable("id") Long id, @Valid @RequestBody VeiculoDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir veículo do catálogo")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Veículo excluído com sucesso"),
        @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    })
    public ResponseEntity<Void> excluir(@PathVariable("id") Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/estatisticas")
    @Operation(summary = "Estatísticas do catálogo de veículos")
    public ResponseEntity<EstatisticasDTO> estatisticas() {
        return ResponseEntity.ok(service.obterEstatisticas());
    }
}
