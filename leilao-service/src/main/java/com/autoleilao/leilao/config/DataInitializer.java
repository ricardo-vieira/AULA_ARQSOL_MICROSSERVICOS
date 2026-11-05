package com.autoleilao.leilao.config;

import com.autoleilao.leilao.command.model.LanceEmbedded;
import com.autoleilao.leilao.command.model.LoteLeilaoDocument;
import com.autoleilao.leilao.command.repository.LoteLeilaoCommandRepository;
import com.autoleilao.leilao.messaging.event.LoteLeilaoSyncEvent;
import com.autoleilao.leilao.messaging.publisher.LeilaoServiceBusGateway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initDatabase(LoteLeilaoCommandRepository commandRepository,
                                          LeilaoServiceBusGateway serviceBusGateway) {
        return args -> {
            if (commandRepository.count() == 0) {
                LoteLeilaoDocument lote1 = new LoteLeilaoDocument(
                        1L, 1L, "Chevrolet", "Onix", 2021, "ABC-1234", "Branco", "Carro",
                        new BigDecimal("45000.00"),
                        "Veículo em ótimo estado, apenas um dono. Placa par, revisões em dia.",
                        3600L
                );

                LoteLeilaoDocument lote2 = new LoteLeilaoDocument(
                        2L, 2L, "Toyota", "Hilux", 2020, "DEF-5678", "Prata", "Pickup",
                        new BigDecimal("180000.00"),
                        "Caminhonete 4x4 com pouco uso. Segundo dono, documentação ok.",
                        7200L
                );
                lote2.addLance(new LanceEmbedded(commandRepository.nextLanceId(), "Pedro L.", new BigDecimal("180000.00"), "14:20"));
                lote2.addLance(new LanceEmbedded(commandRepository.nextLanceId(), "Ana S.", new BigDecimal("182000.00"), "14:28"));
                lote2.addLance(new LanceEmbedded(commandRepository.nextLanceId(), "Carlos M.", new BigDecimal("185000.00"), "14:32"));

                LoteLeilaoDocument lote3 = new LoteLeilaoDocument(
                        3L, 3L, "Honda", "CB 500", 2022, "GHI-9012", "Preto", "Moto",
                        new BigDecimal("28000.00"),
                        "Moto em excelente estado, sem sinistro. Pneus novos, revisão feita.",
                        540L
                );
                lote3.addLance(new LanceEmbedded(commandRepository.nextLanceId(), "Roberto A.", new BigDecimal("28500.00"), "14:48"));
                lote3.addLance(new LanceEmbedded(commandRepository.nextLanceId(), "Fernanda C.", new BigDecimal("29000.00"), "14:55"));
                lote3.addLance(new LanceEmbedded(commandRepository.nextLanceId(), "João B.", new BigDecimal("29500.00"), "15:01"));
                lote3.addLance(new LanceEmbedded(commandRepository.nextLanceId(), "Mariana T.", new BigDecimal("30000.00"), "15:05"));
                lote3.addLance(new LanceEmbedded(commandRepository.nextLanceId(), "Lucas R.", new BigDecimal("31500.00"), "15:10"));

                LoteLeilaoDocument lote4 = new LoteLeilaoDocument(
                        4L, 4L, "Volkswagen", "Polo", 2019, "JKL-3456", "Vermelho", "Carro",
                        new BigDecimal("55000.00"),
                        "Hatch compacto com câmbio automático. Único dono.",
                        1800L
                );
                lote4.addLance(new LanceEmbedded(commandRepository.nextLanceId(), "Juliana P.", new BigDecimal("57000.00"), "15:15"));
                lote4.addLance(new LanceEmbedded(commandRepository.nextLanceId(), "Rafael O.", new BigDecimal("60000.00"), "15:22"));

                for (LoteLeilaoDocument doc : List.of(lote1, lote2, lote3, lote4)) {
                    LoteLeilaoDocument salvo = commandRepository.save(doc);
                    LoteLeilaoSyncEvent ev = new LoteLeilaoSyncEvent();
                    ev.setTipoEvento("LOTE_INICIALIZADO");
                    ev.setId(salvo.getId());
                    ev.setVeiculoId(salvo.getVeiculoId());
                    ev.setMarca(salvo.getMarca());
                    ev.setModelo(salvo.getModelo());
                    ev.setAno(salvo.getAno());
                    ev.setPlaca(salvo.getPlaca());
                    ev.setCor(salvo.getCor());
                    ev.setTipo(salvo.getTipo());
                    ev.setValorMinimo(salvo.getValorMinimo());
                    ev.setDescricao(salvo.getDescricao());
                    ev.setLanceAtual(salvo.getLanceAtual());
                    ev.setTotalLances(salvo.getTotalLances());
                    ev.setTempoRestante(salvo.getTempoRestante());
                    ev.setStatus(salvo.getStatus());
                    ev.setDataCriacao(salvo.getDataCriacao());
                    ev.setBids(salvo.getBids());
                    serviceBusGateway.publicarEventoLote(ev);
                }
            }
        };
    }
}

