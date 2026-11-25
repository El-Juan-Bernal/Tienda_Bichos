package hj.ms_rabbit_admin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Datos que aparecen en la cabecera de Swagger UI. */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openApiRabbitAdmin() {
        return new OpenAPI().info(new Info()
            .title("ms-rabbit-admin")
            .description("Administración de RabbitMQ: colas, exchanges y bindings de los dos brokers "
                + "(principal y proveedores).")
            .version("v1"));
    }
}
