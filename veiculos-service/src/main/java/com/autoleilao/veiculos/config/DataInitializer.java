package com.autoleilao.veiculos.config;

import com.autoleilao.veiculos.model.Veiculo;
import com.autoleilao.veiculos.repository.VeiculoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initDatabase(VeiculoRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                Veiculo v1 = new Veiculo(
                        "Chevrolet", "Onix", 2021, "ABC-1234", "Branco", "Carro",
                        new BigDecimal("45000.00"),
                        "Veículo em ótimo estado, apenas um dono. Placa par, revisões em dia."
                );
                Veiculo v2 = new Veiculo(
                        "Toyota", "Hilux", 2020, "DEF-5678", "Prata", "Pickup",
                        new BigDecimal("180000.00"),
                        "Caminhonete 4x4 com pouco uso. Segundo dono, documentação ok."
                );
                Veiculo v3 = new Veiculo(
                        "Honda", "CB 500", 2022, "GHI-9012", "Preto", "Moto",
                        new BigDecimal("28000.00"),
                        "Moto em excelente estado, sem sinistro. Pneus novos, revisão feita."
                );
                Veiculo v4 = new Veiculo(
                        "Volkswagen", "Polo", 2019, "JKL-3456", "Vermelho", "Carro",
                        new BigDecimal("55000.00"),
                        "Hatch compacto com câmbio automático. Único dono."
                );

                repository.saveAll(List.of(v1, v2, v3, v4));
            }
        };
    }
}
