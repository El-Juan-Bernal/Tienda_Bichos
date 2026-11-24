package hj.ms_pedidos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Datos que aparecen en la cabecera de Swagger UI. */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openApiPedidos() {
        return new OpenAPI().info(new Info()
            .title("ms-pedidos")
            .description("Pedidos de la tienda de insectos H&J: creación, estados, cancelación e historial. "
                + "Publica cada cambio de estado en Kafka (orders.events).")
            .version("v1"));
    }
}

