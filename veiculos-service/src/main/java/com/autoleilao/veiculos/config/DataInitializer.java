package com.autoleilao.veiculos.config;

import com.autoleilao.veiculos.command.repository.VeiculoCommandRepository;
import com.autoleilao.veiculos.command.service.VeiculoCommandService;
import com.autoleilao.veiculos.dto.VeiculoDTO;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initDatabase(VeiculoCommandRepository commandRepository,
                                          VeiculoCommandService commandService) {
        return args -> {
            if (commandRepository.count() == 0) {
                commandService.cadastrarSemNotificarLeilao(criarDto(
                        "Chevrolet", "Onix", 2021, "ABC-1234", "Branco", "Carro",
                        new BigDecimal("45000.00"),
                        "Veículo em ótimo estado, apenas um dono. Placa par, revisões em dia."
                ));
                commandService.cadastrarSemNotificarLeilao(criarDto(
                        "Toyota", "Hilux", 2020, "DEF-5678", "Prata", "Pickup",
                        new BigDecimal("180000.00"),
                        "Caminhonete 4x4 com pouco uso. Segundo dono, documentação ok."
                ));
                commandService.cadastrarSemNotificarLeilao(criarDto(
                        "Honda", "CB 500", 2022, "GHI-9012", "Preto", "Moto",
                        new BigDecimal("28000.00"),
                        "Moto em excelente estado, sem sinistro. Pneus novos, revisão feita."
                ));
                commandService.cadastrarSemNotificarLeilao(criarDto(
                        "Volkswagen", "Polo", 2019, "JKL-3456", "Vermelho", "Carro",
                        new BigDecimal("55000.00"),
                        "Hatch compacto com câmbio automático. Único dono."
                ));
            }
        };
    }

    private VeiculoDTO criarDto(String marca, String modelo, Integer ano, String placa,
                                String cor, String tipo, BigDecimal valorMinimo, String descricao) {
        VeiculoDTO dto = new VeiculoDTO();
        dto.setMarca(marca);
        dto.setModelo(modelo);
        dto.setAno(ano);
        dto.setPlaca(placa);
        dto.setCor(cor);
        dto.setTipo(tipo);
        dto.setValorMinimo(valorMinimo);
        dto.setDescricao(descricao);
        dto.setStatus("DISPONIVEL");
        return dto;
    }
}

