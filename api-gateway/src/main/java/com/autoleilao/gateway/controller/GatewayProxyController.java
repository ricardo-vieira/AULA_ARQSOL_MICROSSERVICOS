package com.autoleilao.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class GatewayProxyController {

    private final RestClient restClient;
    private final String veiculosServiceUrl;
    private final String leilaoServiceUrl;

    public GatewayProxyController(
            @Value("${gateway.services.veiculos-url}") String veiculosServiceUrl,
            @Value("${gateway.services.leilao-url}") String leilaoServiceUrl) {
        this.restClient = RestClient.builder().build();
        this.veiculosServiceUrl = veiculosServiceUrl;
        this.leilaoServiceUrl = leilaoServiceUrl;
    }

    @RequestMapping(value = "/api/veiculos/**")
    public ResponseEntity<?> proxyVeiculos(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        return forwardRequest(request, body, veiculosServiceUrl, "veiculos-service (:8081)");
    }

    @RequestMapping(value = "/api/leiloes/**")
    public ResponseEntity<?> proxyLeiloes(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        return forwardRequest(request, body, leilaoServiceUrl, "leilao-service (:8082)");
    }

    private ResponseEntity<?> forwardRequest(HttpServletRequest request, byte[] body, String targetBaseUrl, String serviceName) {
        String path = request.getRequestURI();
        String queryString = request.getQueryString();
        String targetUrl = targetBaseUrl + path + (queryString != null ? "?" + queryString : "");

        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        try {
            RestClient.RequestBodySpec spec = restClient
                    .method(method)
                    .uri(URI.create(targetUrl));

            if (request.getContentType() != null) {
                spec.contentType(MediaType.parseMediaType(request.getContentType()));
            }

            if (body != null && body.length > 0) {
                spec.body(body);
            }

            return spec.retrieve().toEntity(byte[].class);

        } catch (RestClientResponseException ex) {
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ex.getResponseBodyAsByteArray());

        } catch (ResourceAccessException ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("timestamp", LocalDateTime.now());
            error.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
            error.put("error", "Microsserviço Indisponível");
            error.put("message", "O API Gateway não conseguiu se conectar com o " + serviceName + ". Certifique-se de que o serviço está rodando.");
            error.put("targetUrl", targetUrl);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);

        } catch (Exception ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("timestamp", LocalDateTime.now());
            error.put("status", HttpStatus.BAD_GATEWAY.value());
            error.put("error", "Erro de Roteamento no API Gateway");
            error.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
        }
    }
}
