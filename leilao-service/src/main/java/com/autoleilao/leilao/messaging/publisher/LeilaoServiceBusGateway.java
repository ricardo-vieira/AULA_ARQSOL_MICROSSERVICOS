package com.autoleilao.leilao.messaging.publisher;

import com.autoleilao.leilao.command.service.LeilaoCommandService;
import com.autoleilao.leilao.messaging.consumer.LeilaoQueryProjectionConsumer;
import com.autoleilao.leilao.messaging.event.LoteLeilaoSyncEvent;
import com.autoleilao.leilao.messaging.event.VeiculoCriadoIntegrationEvent;
import com.autoleilao.leilao.model.LoteLeilao;
import com.azure.messaging.servicebus.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class LeilaoServiceBusGateway {

    private static final Logger log = LoggerFactory.getLogger(LeilaoServiceBusGateway.class);

    @Value("${azure.servicebus.connection-string:}")
    private String connectionString;

    @Value("${azure.servicebus.leilao-cqrs-queue:leilao-cqrs-queue}")
    private String cqrsQueueName;

    @Value("${azure.servicebus.veiculos-leilao-queue:veiculos-leilao-queue}")
    private String integrationQueueName;

    private final LeilaoQueryProjectionConsumer projectionConsumer;
    private final LeilaoCommandService commandService;
    private final ObjectMapper objectMapper;

    private ServiceBusSenderClient cqrsSender;
    private ServiceBusProcessorClient cqrsProcessor;
    private ServiceBusProcessorClient integrationProcessor;

    public LeilaoServiceBusGateway(LeilaoQueryProjectionConsumer projectionConsumer,
                                   @Lazy LeilaoCommandService commandService,
                                   ObjectMapper objectMapper) {
        this.projectionConsumer = projectionConsumer;
        this.commandService = commandService;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        if (connectionString != null && !connectionString.isBlank()) {
            try {
                ServiceBusClientBuilder builder = new ServiceBusClientBuilder().connectionString(connectionString.trim());
                this.cqrsSender = builder.sender().queueName(cqrsQueueName).buildClient();

                this.cqrsProcessor = builder.processor()
                        .queueName(cqrsQueueName)
                        .processMessage(this::handleCqrsMessage)
                        .processError(ctx -> log.error("[SERVICE BUS] Erro fila {}: {}", cqrsQueueName, ctx.getException().getMessage()))
                        .buildProcessorClient();

                this.integrationProcessor = builder.processor()
                        .queueName(integrationQueueName)
                        .processMessage(this::handleIntegrationMessage)
                        .processError(ctx -> log.error("[SERVICE BUS] Erro fila {}: {}", integrationQueueName, ctx.getException().getMessage()))
                        .buildProcessorClient();

                this.cqrsProcessor.start();
                this.integrationProcessor.start();
                log.info("[AZURE SERVICE BUS] Conectado às filas '{}' e '{}'", cqrsQueueName, integrationQueueName);
            } catch (Exception e) {
                log.warn("[AZURE SERVICE BUS] Usando fallback local: {}", e.getMessage());
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            if (cqrsProcessor != null) cqrsProcessor.close();
            if (integrationProcessor != null) integrationProcessor.close();
            if (cqrsSender != null) cqrsSender.close();
        } catch (Exception ignored) {
        }
    }

    public boolean isCloudServiceBusActive() {
        return cqrsSender != null;
    }

    public String getCqrsQueueName() {
        return cqrsQueueName;
    }

    public String getIntegrationQueueName() {
        return integrationQueueName;
    }

    public LoteLeilao publicarEventoLote(LoteLeilaoSyncEvent event) {
        LoteLeilao projetado = projectionConsumer.processarEventoSincronizacao(event);

        if (cqrsSender != null) {
            try {
                cqrsSender.sendMessage(new ServiceBusMessage(objectMapper.writeValueAsString(event)));
                log.info("[AZURE SERVICE BUS -> {}] Evento '{}' publicado (Lote ID={})", cqrsQueueName, event.getTipoEvento(), event.getId());
            } catch (Exception e) {
                log.warn("[AZURE SERVICE BUS] Erro ao publicar em {}: {}", cqrsQueueName, e.getMessage());
            }
        }

        return projetado;
    }

    private void handleCqrsMessage(ServiceBusReceivedMessageContext context) {
        try {
            LoteLeilaoSyncEvent event = objectMapper.readValue(context.getMessage().getBody().toString(), LoteLeilaoSyncEvent.class);
            projectionConsumer.processarEventoSincronizacao(event);
        } catch (Exception e) {
            log.error("[AZURE SERVICE BUS] Falha ao processar evento CQRS: {}", e.getMessage());
        }
    }

    private void handleIntegrationMessage(ServiceBusReceivedMessageContext context) {
        try {
            VeiculoCriadoIntegrationEvent event = objectMapper.readValue(
                    context.getMessage().getBody().toString(),
                    VeiculoCriadoIntegrationEvent.class
            );
            commandService.criarLoteAutomaticoPorEventoVeiculo(event);
        } catch (Exception e) {
            log.error("[AZURE SERVICE BUS] Falha ao processar evento de integração: {}", e.getMessage());
        }
    }
}
