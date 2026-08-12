package com.civicaudit.civicaudit_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI civicAuditOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CivicAudit API")
                        .description("API de consultation des marchés publics de la commune de Six-Fours-les-Plages, "
                                + "basée sur les Données Essentielles de la Commande Publique (DECP).")
                        .version("v0.1 (module 3.2 - consultation publique)"));
    }
}