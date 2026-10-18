package br.com.vidaplus.sghss_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração da documentação OpenAPI / Swagger UI.
 * <p>
 * Acesse em: <a href="http://localhost:8080/swagger-ui.html">/swagger-ui.html</a>
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI sghssOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SGHSS API")
                        .description("API REST do Sistema de Gestão Hospitalar e de Serviços de Saúde (VidaPlus).")
                        .version("v1")
                        .contact(new Contact()
                                .name("Cristiano Braga")
                                .url("https://github.com/CristianoBraga88"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
