package com.autoleilao.leilao;

import com.autoleilao.leilao.dto.NovoLanceDTO;
import com.autoleilao.leilao.model.LoteLeilao;
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

    @Test
    void deveListarLotesIniciais() {
        List<LoteLeilao> lotes = leilaoService.listarLotes(null);
        assertNotNull(lotes);
        assertEquals(4, lotes.size());
    }

    @Test
    void deveDarLanceComSucessoEAtualizarValor() {
        // Lote 1 tem lanceAtual = 45000.00
        NovoLanceDTO novoLance = new NovoLanceDTO("Professor Teste", new BigDecimal("46000.00"));
        LoteLeilao atualizado = leilaoService.darLance(1L, novoLance);

        assertEquals(new BigDecimal("46000.00"), atualizado.getLanceAtual());
        assertEquals(1, atualizado.getTotalLances());
        assertFalse(atualizado.getBids().isEmpty());
        assertEquals("Professor Teste", atualizado.getBids().get(0).getBidder());
    }

    @Test
    void deveRejeitarLanceAbaixoDoMinimo() {
        // Lote 2 tem lanceAtual 185000. Lance mínimo deve ser 185500.
        NovoLanceDTO lanceInvalido = new NovoLanceDTO("Infrator", new BigDecimal("185100.00"));
        assertThrows(RuntimeException.class, () -> leilaoService.darLance(2L, lanceInvalido));
    }
}
