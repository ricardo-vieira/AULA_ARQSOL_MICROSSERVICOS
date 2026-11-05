package com.autoleilao.leilao.config;

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
                        .title("AutoLeilão — Microsserviço de Leilão e Lances (CQRS + Database per Service)")
                        .version("2.0.0")
                        .description("Arquitetura CQRS + Database per Service + Aggregate Root:\n" +
                                "• COMMAND (Escrita de Baixa Latência): Azure Cosmos DB for MongoDB (Database exclusivo: leilao_command_db, Aggregate Root com @Version Optimistic Locking)\n" +
                                "• MENSAGERIA (Sincronização e Integração): Azure Service Bus (leilao-cqrs-queue e veiculos-leilao-queue)\n" +
                                "• QUERY (Leitura Relacional): Azure SQL Server (Tabelas: lotes_leilao_query e lances_query)")
                        .contact(new Contact().name("Disciplina de Arquitetura de Soluções")));
    }
}

