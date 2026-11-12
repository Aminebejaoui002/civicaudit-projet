package com.civicaudit.civicaudit_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI civicAuditOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CivicAudit API")
                        .description("Plateforme de co-audit citoyen des marchés publics de la commune de "
                                + "Six-Fours-les-Plages, basée sur les Données Essentielles de la Commande Publique (DECP). "
                                + "Pour tester les routes protégées : POST /api/auth/login, puis bouton « Authorize » avec le token.")
                        .version("v1.0"))
                // Bouton « Authorize » dans Swagger UI pour envoyer le JWT
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
