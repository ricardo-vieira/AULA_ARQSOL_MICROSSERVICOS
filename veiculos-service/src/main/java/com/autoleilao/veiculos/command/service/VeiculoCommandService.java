package com.autoleilao.veiculos.command.service;

import com.autoleilao.veiculos.command.model.VeiculoDocument;
import com.autoleilao.veiculos.command.repository.VeiculoCommandRepository;
import com.autoleilao.veiculos.dto.VeiculoDTO;
import com.autoleilao.veiculos.exception.RecursoNaoEncontradoException;
import com.autoleilao.veiculos.exception.RegraNegocioException;
import com.autoleilao.veiculos.messaging.event.VeiculoSyncEvent;
import com.autoleilao.veiculos.messaging.publisher.VeiculoServiceBusGateway;
import com.autoleilao.veiculos.model.Veiculo;
import org.springframework.stereotype.Service;

/**
 * Serviço de Escrita (COMMAND) do padrão CQRS para Veículos.
 * Responsável exclusivamente por mutações de estado (Create, Update, Delete)
 * no Azure Cosmos DB (database: veiculos_command_db) e disparo de eventos no Azure Service Bus.
 */
@Service
public class VeiculoCommandService {

    private final VeiculoCommandRepository commandRepository;
    private final VeiculoServiceBusGateway serviceBusGateway;

    public VeiculoCommandService(VeiculoCommandRepository commandRepository,
                                 VeiculoServiceBusGateway serviceBusGateway) {
        this.commandRepository = commandRepository;
        this.serviceBusGateway = serviceBusGateway;
    }

    public Veiculo cadastrar(VeiculoDTO dto) {
        return cadastrarInterno(dto, true);
    }

    public Veiculo cadastrarSemNotificarLeilao(VeiculoDTO dto) {
        return cadastrarInterno(dto, false);
    }

    private Veiculo cadastrarInterno(VeiculoDTO dto, boolean notificarLeilaoService) {
        String placaFormatada = dto.getPlaca().toUpperCase().trim();
        if (commandRepository.existsByPlacaIgnoreCase(placaFormatada)) {
            throw new RegraNegocioException("Já existe um veículo cadastrado com a placa " + placaFormatada);
        }

        VeiculoDocument doc = new VeiculoDocument();
        copiarDadosParaDocumento(dto, doc);

        // 1. Grava no banco de COMMAND (Azure Cosmos DB / MongoDB - veiculos_command_db)
        VeiculoDocument salvo = commandRepository.save(doc);

        // 2. Publica evento no Azure Service Bus para atualizar o Read Model (Azure SQL) e criar lote no Leilão
        VeiculoSyncEvent event = toSyncEvent("CRIADO", salvo);
        serviceBusGateway.publicarEventoVeiculo(event, notificarLeilaoService);

        return toViewModel(salvo);
    }

    public Veiculo atualizar(Long id, VeiculoDTO dto) {
        VeiculoDocument doc = commandRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo com ID " + id + " não encontrado no banco de Command."));

        String placaFormatada = dto.getPlaca().toUpperCase().trim();
        if (commandRepository.existsByPlacaIgnoreCaseAndIdNot(placaFormatada, id)) {
            throw new RegraNegocioException("Já existe outro veículo cadastrado com a placa " + placaFormatada);
        }

        copiarDadosParaDocumento(dto, doc);

        // 1. Atualiza no banco de COMMAND (Azure Cosmos DB)
        VeiculoDocument atualizado = commandRepository.save(doc);

        // 2. Publica evento de sincronização no Azure Service Bus para atualizar o Azure SQL Server
        VeiculoSyncEvent event = toSyncEvent("ATUALIZADO", atualizado);
        serviceBusGateway.publicarEventoVeiculo(event, false);

        return toViewModel(atualizado);
    }

    public void excluir(Long id) {
        VeiculoDocument doc = commandRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo com ID " + id + " não encontrado no banco de Command."));

        // 1. Remove do banco de COMMAND (Azure Cosmos DB)
        commandRepository.deleteById(id);

        // 2. Publica evento de exclusão no Azure Service Bus para remover do Azure SQL Server
        VeiculoSyncEvent event = toSyncEvent("EXCLUIDO", doc);
        serviceBusGateway.publicarEventoVeiculo(event, false);
    }

    private void copiarDadosParaDocumento(VeiculoDTO dto, VeiculoDocument doc) {
        doc.setMarca(dto.getMarca());
        doc.setModelo(dto.getModelo());
        doc.setAno(dto.getAno());
        doc.setPlaca(dto.getPlaca().toUpperCase().trim());
        doc.setCor(dto.getCor());
        doc.setTipo(dto.getTipo());
        doc.setValorMinimo(dto.getValorMinimo());
        doc.setDescricao(dto.getDescricao());
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            doc.setStatus(dto.getStatus());
        } else if (doc.getStatus() == null || doc.getStatus().isBlank()) {
            doc.setStatus("DISPONIVEL");
        }
    }

    private VeiculoSyncEvent toSyncEvent(String tipoEvento, VeiculoDocument doc) {
        return new VeiculoSyncEvent(
                tipoEvento,
                doc.getId(),
                doc.getMarca(),
                doc.getModelo(),
                doc.getAno(),
                doc.getPlaca(),
                doc.getCor(),
                doc.getTipo(),
                doc.getValorMinimo(),
                doc.getDescricao(),
                doc.getStatus(),
                doc.getDataCadastro()
        );
    }

    private Veiculo toViewModel(VeiculoDocument doc) {
        Veiculo v = new Veiculo(
                doc.getMarca(),
                doc.getModelo(),
                doc.getAno(),
                doc.getPlaca(),
                doc.getCor(),
                doc.getTipo(),
                doc.getValorMinimo(),
                doc.getDescricao()
        );
        v.setId(doc.getId());
        v.setStatus(doc.getStatus());
        v.setDataCadastro(doc.getDataCadastro());
        return v;
    }
}
