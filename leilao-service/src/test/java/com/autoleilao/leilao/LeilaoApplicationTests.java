package com.autoleilao.leilao;

import com.autoleilao.leilao.command.model.LoteLeilaoDocument;
import com.autoleilao.leilao.command.repository.LoteLeilaoCommandRepository;
import com.autoleilao.leilao.command.service.LeilaoCommandService;
import com.autoleilao.leilao.dto.NovoLanceDTO;
import com.autoleilao.leilao.messaging.event.VeiculoCriadoIntegrationEvent;
import com.autoleilao.leilao.model.LoteLeilao;
import com.autoleilao.leilao.query.service.LeilaoQueryService;
import com.autoleilao.leilao.service.LeilaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LeilaoApplicationTests {

    @Autowired
    private LeilaoService leilaoService;

    @Autowired
    private LeilaoCommandService commandService;

    @Autowired
    private LeilaoQueryService queryService;

    @Autowired
    private LoteLeilaoCommandRepository commandRepository;

    @Test
    void deveListarLotesIniciaisSincronizadosNoReadModel() {
        List<LoteLeilao> lotes = leilaoService.listarLotes(null);
        assertNotNull(lotes);
        assertTrue(lotes.size() >= 4);
        assertEquals(commandRepository.count(), queryService.countReadModel());
    }

    @Test
    void deveDarLanceNoCommandAtualizarVersaoESincronizarReadModel() {
        LoteLeilaoDocument antesDoc = commandRepository.findById(1L).orElseThrow();
        Long versaoAnterior = antesDoc.getVersion();

        NovoLanceDTO novoLance = new NovoLanceDTO("Professor Teste CQRS", new BigDecimal("46000.00"));
        LoteLeilao atualizado = leilaoService.darLance(1L, novoLance);

        assertEquals(new BigDecimal("46000.00"), atualizado.getLanceAtual());
        assertEquals(1, atualizado.getTotalLances());
        assertFalse(atualizado.getBids().isEmpty());
        assertEquals("Professor Teste CQRS", atualizado.getBids().get(0).getBidder());

        // Verifica que a versão (@Version Optimistic Locking) subiu no Command DB (CosmosDB)
        LoteLeilaoDocument depoisDoc = commandRepository.findById(1L).orElseThrow();
        assertTrue(depoisDoc.getVersion() > versaoAnterior);

        // Verifica que o Read Model (Azure SQL Server) também foi sincronizado
        LoteLeilao lidoDoSql = queryService.buscarPorId(1L);
        assertEquals(new BigDecimal("46000.00"), lidoDoSql.getLanceAtual());
    }

    @Test
    void deveRejeitarLanceAbaixoDoMinimo() {
        NovoLanceDTO lanceInvalido = new NovoLanceDTO("Infrator", new BigDecimal("185100.00"));
        assertThrows(RuntimeException.class, () -> leilaoService.darLance(2L, lanceInvalido));
    }

    @Test
    void deveCriarLoteAutomaticamenteAoReceberEventoDeVeiculoCadastrado() {
        VeiculoCriadoIntegrationEvent evento = new VeiculoCriadoIntegrationEvent();
        evento.setId(99L);
        evento.setMarca("Porsche");
        evento.setModelo("911 Carrera");
        evento.setAno(2024);
        evento.setPlaca("POR-9911");
        evento.setCor("Azul");
        evento.setTipo("Carro");
        evento.setValorMinimo(new BigDecimal("850000.00"));
        evento.setDescricao("Criado via evento do Azure Service Bus");

        commandService.criarLoteAutomaticoPorEventoVeiculo(evento);

        assertTrue(commandRepository.existsByPlacaIgnoreCase("POR-9911"));
        boolean presenteNoReadModel = queryService.listarLotes("ativo").stream()
                .anyMatch(l -> "POR-9911".equalsIgnoreCase(l.getPlaca()));
        assertTrue(presenteNoReadModel);
    }
}

