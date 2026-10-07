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
                        .title("AutoLeilão — Microsserviço de Leilão e Lances")
                        .version("1.0.0")
                        .description("API REST do Microsserviço de Pregão e Lances para a disciplina de Arquitetura de Soluções. " +
                                "Demonstra regras de negócio de lances em tempo real, isolamento de banco de dados e integração assíncrona/desacoplada.")
                        .contact(new Contact().name("Disciplina de Arquitetura de Soluções")));
    }
}
