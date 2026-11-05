package com.autoleilao.veiculos;

import com.autoleilao.veiculos.command.repository.VeiculoCommandRepository;
import com.autoleilao.veiculos.dto.VeiculoDTO;
import com.autoleilao.veiculos.model.Veiculo;
import com.autoleilao.veiculos.query.service.VeiculoQueryService;
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

    @Autowired
    private VeiculoCommandRepository commandRepository;

    @Autowired
    private VeiculoQueryService queryService;

    @Test
    void deveListarVeiculosIniciaisSincronizadosNoReadModel() {
        List<Veiculo> lista = veiculoService.listarTodos(null, null);
        assertNotNull(lista);
        assertFalse(lista.isEmpty());
        assertEquals(commandRepository.count(), queryService.countReadModel());
    }

    @Test
    void deveCadastrarNoCommandSincronizarNoQueryENaoPermitirPlacaDuplicada() {
        VeiculoDTO dto = new VeiculoDTO();
        dto.setMarca("Ford");
        dto.setModelo("Ka");
        dto.setAno(2020);
        dto.setPlaca("TST-9999");
        dto.setCor("Prata");
        dto.setTipo("Carro");
        dto.setValorMinimo(new BigDecimal("35000.00"));
        dto.setDescricao("Teste unitário CQRS");

        Veiculo criado = veiculoService.cadastrar(dto);
        assertNotNull(criado.getId());
        assertEquals("TST-9999", criado.getPlaca());

        // Verifica se existe tanto no Command DB (CosmosDB/Document) quanto no Query DB (SQL Server)
        assertTrue(commandRepository.findById(criado.getId()).isPresent());
        Veiculo projetadoNoSql = queryService.buscarPorId(criado.getId());
        assertEquals("TST-9999", projetadoNoSql.getPlaca());

        // Testar duplicidade no Command DB
        assertThrows(RuntimeException.class, () -> veiculoService.cadastrar(dto));
    }
}

