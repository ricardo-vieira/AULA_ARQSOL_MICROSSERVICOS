package com.autoleilao.leilao.repository;

import com.autoleilao.leilao.model.Lance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LanceRepository extends JpaRepository<Lance, Long> {

    List<Lance> findByLoteIdOrderByDataHoraDesc(Long loteId);
}
