package com.autoleilao.veiculos.messaging.publisher;

import com.autoleilao.veiculos.messaging.consumer.VeiculoQueryProjectionConsumer;
import com.autoleilao.veiculos.messaging.event.VeiculoSyncEvent;
import com.azure.messaging.servicebus.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@Component
public class VeiculoServiceBusGateway {

    private static final Logger log = LoggerFactory.getLogger(VeiculoServiceBusGateway.class);

    @Value("${azure.servicebus.connection-string:}")
    private String connectionString;

    @Value("${azure.servicebus.veiculos-cqrs-queue:veiculos-cqrs-queue}")
    private String cqrsQueueName;

    @Value("${azure.servicebus.veiculos-leilao-queue:veiculos-leilao-queue}")
    private String integrationQueueName;

    @Value("${autoleilao.leilao-service.url:http://localhost:8082}")
    private String leilaoServiceUrl;

    private final VeiculoQueryProjectionConsumer projectionConsumer;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    private ServiceBusSenderClient cqrsSender;
    private ServiceBusSenderClient integrationSender;
    private ServiceBusProcessorClient cqrsProcessor;

    public VeiculoServiceBusGateway(VeiculoQueryProjectionConsumer projectionConsumer, ObjectMapper objectMapper) {
        this.projectionConsumer = projectionConsumer;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        if (connectionString != null && !connectionString.isBlank()) {
            try {
                ServiceBusClientBuilder builder = new ServiceBusClientBuilder().connectionString(connectionString.trim());
                this.cqrsSender = builder.sender().queueName(cqrsQueueName).buildClient();
                this.integrationSender = builder.sender().queueName(integrationQueueName).buildClient();
                this.cqrsProcessor = builder.processor()
                        .queueName(cqrsQueueName)
                        .processMessage(this::handleMessage)
                        .processError(ctx -> log.error("[SERVICE BUS] Erro: {}", ctx.getException().getMessage()))
                        .buildProcessorClient();
                this.cqrsProcessor.start();
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
            if (cqrsSender != null) cqrsSender.close();
            if (integrationSender != null) integrationSender.close();
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

    public void publicarEventoVeiculo(VeiculoSyncEvent event, boolean notificarLeilaoService) {
        projectionConsumer.processarEventoSincronizacao(event);

        if (cqrsSender != null) {
            try {
                cqrsSender.sendMessage(new ServiceBusMessage(objectMapper.writeValueAsString(event)));
            } catch (Exception e) {
                log.warn("[AZURE SERVICE BUS] Erro ao publicar em {}: {}", cqrsQueueName, e.getMessage());
            }
        }

        if (notificarLeilaoService && "CRIADO".equalsIgnoreCase(event.getTipoEvento())) {
            publicarIntegracaoLeilao(event);
        }
    }

    private void publicarIntegracaoLeilao(VeiculoSyncEvent event) {
        if (integrationSender != null) {
            try {
                integrationSender.sendMessage(new ServiceBusMessage(objectMapper.writeValueAsString(event)));
                log.info("[AZURE SERVICE BUS -> {}] Evento enviado para abrir lote (Placa={})", integrationQueueName, event.getPlaca());
                return;
            } catch (Exception e) {
                log.warn("[AZURE SERVICE BUS] Erro na fila {}: {}", integrationQueueName, e.getMessage());
            }
        }

        CompletableFuture.runAsync(() -> {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                restTemplate.postForEntity(
                        leilaoServiceUrl + "/api/leiloes/internal/eventos/veiculo-criado",
                        new HttpEntity<>(event, headers),
                        Void.class
                );
            } catch (Exception ignored) {
            }
        });
    }

    private void handleMessage(ServiceBusReceivedMessageContext context) {
        try {
            VeiculoSyncEvent event = objectMapper.readValue(context.getMessage().getBody().toString(), VeiculoSyncEvent.class);
            projectionConsumer.processarEventoSincronizacao(event);
        } catch (Exception e) {
            log.error("[AZURE SERVICE BUS] Falha ao processar evento: {}", e.getMessage());
        }
    }
}
