package com.autoleilao.veiculos.service;

import com.autoleilao.veiculos.dto.EstatisticasDTO;
import com.autoleilao.veiculos.dto.VeiculoDTO;
import com.autoleilao.veiculos.exception.RecursoNaoEncontradoException;
import com.autoleilao.veiculos.exception.RegraNegocioException;
import com.autoleilao.veiculos.model.Veiculo;
import com.autoleilao.veiculos.repository.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VeiculoService {

    private final VeiculoRepository repository;

    public VeiculoService(VeiculoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Veiculo> listarTodos(String tipo, String marca) {
        if (tipo != null && !tipo.isBlank()) {
            return repository.findByTipoIgnoreCase(tipo.trim());
        }
        if (marca != null && !marca.isBlank()) {
            return repository.findByMarcaIgnoreCase(marca.trim());
        }
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Veiculo buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo com ID " + id + " não encontrado."));
    }

    @Transactional
    public Veiculo cadastrar(VeiculoDTO dto) {
        String placaFormatada = dto.getPlaca().toUpperCase().trim();
        if (repository.existsByPlacaIgnoreCase(placaFormatada)) {
            throw new RegraNegocioException("Já existe um veículo cadastrado com a placa " + placaFormatada);
        }

        Veiculo veiculo = new Veiculo();
        copiarDados(dto, veiculo);
        return repository.save(veiculo);
    }

    @Transactional
    public Veiculo atualizar(Long id, VeiculoDTO dto) {
        Veiculo veiculo = buscarPorId(id);
        String placaFormatada = dto.getPlaca().toUpperCase().trim();

        if (repository.existsByPlacaIgnoreCaseAndIdNot(placaFormatada, id)) {
            throw new RegraNegocioException("Já existe outro veículo cadastrado com a placa " + placaFormatada);
        }

        copiarDados(dto, veiculo);
        return repository.save(veiculo);
    }

    @Transactional
    public void excluir(Long id) {
        Veiculo veiculo = buscarPorId(id);
        repository.delete(veiculo);
    }

    @Transactional(readOnly = true)
    public EstatisticasDTO obterEstatisticas() {
        List<Veiculo> todos = repository.findAll();
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

    private void copiarDados(VeiculoDTO dto, Veiculo entidade) {
        entidade.setMarca(dto.getMarca());
        entidade.setModelo(dto.getModelo());
        entidade.setAno(dto.getAno());
        entidade.setPlaca(dto.getPlaca().toUpperCase().trim());
        entidade.setCor(dto.getCor());
        entidade.setTipo(dto.getTipo());
        entidade.setValorMinimo(dto.getValorMinimo());
        entidade.setDescricao(dto.getDescricao());
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            entidade.setStatus(dto.getStatus());
        }
    }
}
