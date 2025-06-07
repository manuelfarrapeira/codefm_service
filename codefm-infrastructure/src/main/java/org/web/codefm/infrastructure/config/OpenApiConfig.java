package org.web.codefm.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI (Swagger) documentation settings.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates a custom OpenAPI configuration with basic authentication.
     *
     * @return Configured OpenAPI instance with security schemes and API information
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("basicAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP).scheme("basic")))
                .info(new Info().title("API Documentation").version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"));
    }
}
