package com.autoleilao.veiculos.repository;

import com.autoleilao.veiculos.model.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    Optional<Veiculo> findByPlacaIgnoreCase(String placa);

    boolean existsByPlacaIgnoreCase(String placa);

    boolean existsByPlacaIgnoreCaseAndIdNot(String placa, Long id);

    List<Veiculo> findByTipoIgnoreCase(String tipo);

    List<Veiculo> findByMarcaIgnoreCase(String marca);
}
