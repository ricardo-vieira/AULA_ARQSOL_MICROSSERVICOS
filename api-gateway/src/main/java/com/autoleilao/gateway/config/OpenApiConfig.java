package com.autoleilao.gateway.config;

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
                        .title("AutoLeilão — API Gateway Central")
                        .version("1.0.0")
                        .description("API Gateway unificado para a arquitetura de microsserviços do AutoLeilão. " +
                                "Roteia requisições do Shell/MFEs para os microsserviços de Veículos (:8081) e Leilão (:8082).")
                        .contact(new Contact().name("Disciplina de Arquitetura de Soluções")));
    }
}
