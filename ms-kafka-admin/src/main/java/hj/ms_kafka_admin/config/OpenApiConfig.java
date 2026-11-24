package hj.ms_kafka_admin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Datos que aparecen en la cabecera de Swagger UI. */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openApiKafkaAdmin() {
        return new OpenAPI().info(new Info()
            .title("ms-kafka-admin")
            .description("Administración del clúster Kafka: tópicos, configuraciones y particiones.")
            .version("v1"));
    }
}

