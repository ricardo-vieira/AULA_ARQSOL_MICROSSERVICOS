package com.autoleilao.veiculos;

import com.autoleilao.veiculos.dto.VeiculoDTO;
import com.autoleilao.veiculos.model.Veiculo;
import com.autoleilao.veiculos.service.VeiculoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class VeiculosApplicationTests {

    @Autowired
    private VeiculoService veiculoService;

    @Test
    void deveListarVeiculosIniciais() {
        List<Veiculo> lista = veiculoService.listarTodos(null, null);
        assertNotNull(lista);
        assertFalse(lista.isEmpty());
    }

    @Test
    void deveCadastrarENaoPermitirPlacaDuplicada() {
        VeiculoDTO dto = new VeiculoDTO();
        dto.setMarca("Ford");
        dto.setModelo("Ka");
        dto.setAno(2020);
        dto.setPlaca("TST-9999");
        dto.setCor("Prata");
        dto.setTipo("Carro");
        dto.setValorMinimo(new BigDecimal("35000.00"));
        dto.setDescricao("Teste unitário");

        Veiculo criado = veiculoService.cadastrar(dto);
        assertNotNull(criado.getId());
        assertEquals("TST-9999", criado.getPlaca());

        // Testar duplicidade
        assertThrows(RuntimeException.class, () -> veiculoService.cadastrar(dto));
    }
}
