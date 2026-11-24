package hj.ms_catalogo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Datos que aparecen en la cabecera de Swagger UI. */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openApiCatalogo() {
        return new OpenAPI().info(new Info()
            .title("ms-catalogo")
            .description("Catálogo de productos, stock y destacados de la tienda de insectos H&J.")
            .version("v1"));
    }
}

