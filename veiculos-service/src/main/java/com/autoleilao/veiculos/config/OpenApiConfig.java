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
                        .title("AutoLeilão — Microsserviço de Veículos")
                        .version("1.0.0")
                        .description("API REST do Microsserviço de Veículos para a disciplina de Arquitetura de Soluções. " +
                                "Demonstra operações de CRUD, isolamento de banco de dados (Database-per-Service) e validações de domínio.")
                        .contact(new Contact().name("Disciplina de Arquitetura de Soluções")));
    }
}
