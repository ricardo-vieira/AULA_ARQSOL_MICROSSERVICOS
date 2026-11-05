package com.autoleilao.veiculos.query.service;

import com.autoleilao.veiculos.dto.EstatisticasDTO;
import com.autoleilao.veiculos.exception.RecursoNaoEncontradoException;
import com.autoleilao.veiculos.model.Veiculo;
import com.autoleilao.veiculos.repository.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Serviço de Leitura (QUERY) do padrão CQRS para Veículos.
 * Consulta exclusivamente o banco relacional de leitura (Azure SQL Server - tabela veiculos_query),
 * garantindo zero contenção com o banco de escrita (CosmosDB).
 */
@Service
public class VeiculoQueryService {

    private final VeiculoRepository queryRepository;

    public VeiculoQueryService(VeiculoRepository queryRepository) {
        this.queryRepository = queryRepository;
    }

    @Transactional(readOnly = true)
    public List<Veiculo> listarTodos(String tipo, String marca) {
        if (tipo != null && !tipo.isBlank()) {
            return queryRepository.findByTipoIgnoreCase(tipo.trim());
        }
        if (marca != null && !marca.isBlank()) {
            return queryRepository.findByMarcaIgnoreCase(marca.trim());
        }
        return queryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Veiculo buscarPorId(Long id) {
        return queryRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo com ID " + id + " não encontrado no Read Model (SQL Server)."));
    }

    @Transactional(readOnly = true)
    public EstatisticasDTO obterEstatisticas() {
        List<Veiculo> todos = queryRepository.findAll();
        long total = todos.size();
        BigDecimal valorTotal = todos.stream()
                .map(Veiculo::getValorMinimo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> porTipo = todos.stream()
                .collect(Collectors.groupingBy(Veiculo::getTipo, Collectors.counting()));

        Map<String, Long> porMarca = todos.stream()
                .collect(Collectors.groupingBy(Veiculo::getMarca, Collectors.counting()));

        return new EstatisticasDTO(total, valorTotal, porTipo, porMarca);
    }

    @Transactional(readOnly = true)
    public long countReadModel() {
        return queryRepository.count();
    }
}
