package com.autoleilao.veiculos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AutoLeilão — Microsserviço de Veículos (CQRS + Database per Service)")
                        .version("2.0.0")
                        .description("Arquitetura CQRS + Database per Service:\n" +
                                "• COMMAND (Escrita): Azure Cosmos DB for MongoDB (Database exclusivo: veiculos_command_db)\n" +
                                "• MENSAGERIA (Sincronização e Integração): Azure Service Bus (veiculos-cqrs-queue e veiculos-leilao-queue)\n" +
                                "• QUERY (Leitura): Azure SQL Server (Tabela otimizada: veiculos_query)")
                        .contact(new Contact().name("Disciplina de Arquitetura de Soluções")));
    }
}

