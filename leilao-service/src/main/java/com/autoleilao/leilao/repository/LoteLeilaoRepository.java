package com.autoleilao.leilao.repository;

import com.autoleilao.leilao.model.LoteLeilao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoteLeilaoRepository extends JpaRepository<LoteLeilao, Long> {

    List<LoteLeilao> findByStatusIgnoreCase(String status);
}
