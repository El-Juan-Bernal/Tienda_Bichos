package hj.bff.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Datos de la cabecera de Swagger UI y botón "Authorize" para pegar el token de Entra ID. */
@Configuration
public class OpenApiConfig {

  private static final String ESQUEMA_JWT = "jwt-entra";

  @Bean
  OpenAPI openApiBff() {
    return new OpenAPI()
        .info(new Info()
            .title("bff - Insectos H&J")
            .description("Punto de entrada del front. Valida el JWT de Microsoft Entra ID, aplica los roles "
                + "y reenvía cada petición al microservicio correspondiente.")
            .version("v1"))
        .components(new Components().addSecuritySchemes(ESQUEMA_JWT,
            new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Access token de Microsoft Entra ID (pégalo sin la palabra Bearer).")))
        .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT));
  }
}
