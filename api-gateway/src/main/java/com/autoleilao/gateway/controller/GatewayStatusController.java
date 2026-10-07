package com.autoleilao.gateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/gateway")
@Tag(name = "Gateway Status", description = "Monitoramento e status de saúde dos microsserviços integrados")
@CrossOrigin(origins = "*")
public class GatewayStatusController {

    private final RestClient restClient;
    private final String veiculosServiceUrl;
    private final String leilaoServiceUrl;

    public GatewayStatusController(
            @Value("${gateway.services.veiculos-url}") String veiculosServiceUrl,
            @Value("${gateway.services.leilao-url}") String leilaoServiceUrl) {
        this.restClient = RestClient.builder().build();
        this.veiculosServiceUrl = veiculosServiceUrl;
        this.leilaoServiceUrl = leilaoServiceUrl;
    }

    @GetMapping("/status")
    @Operation(summary = "Status de saúde do ecossistema", description = "Verifica se os microsserviços de Veículos e Leilão estão respondendo")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> response = new HashMap<>();
        response.put("gateway", "AutoLeilão API Gateway");
        response.put("timestamp", LocalDateTime.now());

        Map<String, Object> services = new HashMap<>();
        services.put("veiculos-service", checkHealth(veiculosServiceUrl + "/actuator/health", "http://localhost:8081"));
        services.put("leilao-service", checkHealth(leilaoServiceUrl + "/actuator/health", "http://localhost:8082"));

        response.put("services", services);
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> checkHealth(String healthUrl, String baseUrl) {
        Map<String, Object> serviceInfo = new HashMap<>();
        serviceInfo.put("url", baseUrl);
        try {
            String health = restClient.get().uri(healthUrl).retrieve().body(String.class);
            serviceInfo.put("status", "UP");
            serviceInfo.put("details", health);
        } catch (Exception e) {
            serviceInfo.put("status", "DOWN");
            serviceInfo.put("error", "Não foi possível conectar ao serviço.");
        }
        return serviceInfo;
    }
}
