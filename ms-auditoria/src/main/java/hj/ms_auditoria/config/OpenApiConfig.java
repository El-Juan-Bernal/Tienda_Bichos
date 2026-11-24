package hj.ms_auditoria.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Datos que aparecen en la cabecera de Swagger UI. */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openApiAuditoria() {
        return new OpenAPI().info(new Info()
            .title("ms-auditoria")
            .description("Línea de tiempo de los eventos de pedidos (solo lectura). "
                + "Los eventos se consumen desde Kafka (orders.events) y se guardan en MySQL.")
            .version("v1"));
    }
}

