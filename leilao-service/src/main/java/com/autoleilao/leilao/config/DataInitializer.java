package com.autoleilao.leilao.config;

import com.autoleilao.leilao.model.Lance;
import com.autoleilao.leilao.model.LoteLeilao;
import com.autoleilao.leilao.repository.LoteLeilaoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initDatabase(LoteLeilaoRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                // Lote 1: Onix
                LoteLeilao lote1 = new LoteLeilao(
                        1L, "Chevrolet", "Onix", 2021, "ABC-1234", "Branco", "Carro",
                        new BigDecimal("45000.00"),
                        "Veículo em ótimo estado, apenas um dono. Placa par, revisões em dia.",
                        3600L
                );

                // Lote 2: Hilux
                LoteLeilao lote2 = new LoteLeilao(
                        2L, "Toyota", "Hilux", 2020, "DEF-5678", "Prata", "Pickup",
                        new BigDecimal("180000.00"),
                        "Caminhonete 4x4 com pouco uso. Segundo dono, documentação ok.",
                        7200L
                );
                lote2.addLance(new Lance("Pedro L.", new BigDecimal("180000.00"), lote2, "14:20"));
                lote2.addLance(new Lance("Ana S.", new BigDecimal("182000.00"), lote2, "14:28"));
                lote2.addLance(new Lance("Carlos M.", new BigDecimal("185000.00"), lote2, "14:32"));

                // Lote 3: CB 500
                LoteLeilao lote3 = new LoteLeilao(
                        3L, "Honda", "CB 500", 2022, "GHI-9012", "Preto", "Moto",
                        new BigDecimal("28000.00"),
                        "Moto em excelente estado, sem sinistro. Pneus novos, revisão feita.",
                        540L
                );
                lote3.addLance(new Lance("Roberto A.", new BigDecimal("28500.00"), lote3, "14:48"));
                lote3.addLance(new Lance("Fernanda C.", new BigDecimal("29000.00"), lote3, "14:55"));
                lote3.addLance(new Lance("João B.", new BigDecimal("29500.00"), lote3, "15:01"));
                lote3.addLance(new Lance("Mariana T.", new BigDecimal("30000.00"), lote3, "15:05"));
                lote3.addLance(new Lance("Lucas R.", new BigDecimal("31500.00"), lote3, "15:10"));

                // Lote 4: Polo
                LoteLeilao lote4 = new LoteLeilao(
                        4L, "Volkswagen", "Polo", 2019, "JKL-3456", "Vermelho", "Carro",
                        new BigDecimal("55000.00"),
                        "Hatch compacto com câmbio automático. Único dono.",
                        1800L
                );
                lote4.addLance(new Lance("Juliana P.", new BigDecimal("57000.00"), lote4, "15:15"));
                lote4.addLance(new Lance("Rafael O.", new BigDecimal("60000.00"), lote4, "15:22"));

                repository.saveAll(List.of(lote1, lote2, lote3, lote4));
            }
        };
    }
}
