package hj.ms_reportes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Datos que aparecen en la cabecera de Swagger UI. */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openApiReportes() {
        return new OpenAPI().info(new Info()
            .title("ms-reportes")
            .description("KPIs y top de productos calculados en tiempo real con Kafka Streams (solo lectura).")
            .version("v1"));
    }
}

