package com.autoleilao.veiculos.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Configuração do Azure Cosmos DB for MongoDB para a camada de COMMAND (Escrita).
 * Conecta ao database exclusivo 'veiculos_command_db' quando AZURE_COSMOS_URI está configurado.
 */
@Configuration
public class CosmosMongoConfig {

    private static final Logger log = LoggerFactory.getLogger(CosmosMongoConfig.class);

    @Value("${azure.cosmos.uri:}")
    private String cosmosUri;

    @Value("${azure.cosmos.database:veiculos_command_db}")
    private String databaseName;

    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnExpression("!'${azure.cosmos.uri:}'.trim().isEmpty()")
    public MongoTemplate cosmosMongoTemplate() {
        log.info("[CQRS-COMMAND] Conectando ao Azure Cosmos DB (MongoDB API) - Database isolado: {}", databaseName);
        MongoClient client = MongoClients.create(cosmosUri.trim());
        return new MongoTemplate(client, databaseName);
    }
}
