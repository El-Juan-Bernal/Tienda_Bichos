package hj.ms_kafka_admin.config;

import hj.ms_kafka_admin.service.KafkaAdminService;
import java.util.List;
import org.apache.kafka.clients.admin.Admin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaAdminConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaAdminConfig.class);

    @Bean(destroyMethod = "close")
    Admin kafkaAdminClient(KafkaAdmin kafkaAdmin) {
        return Admin.create(kafkaAdmin.getConfigurationProperties());
    }

    /** Crea los tópicos base si no existen. Si Kafka no responde, solo avisa. */
    @Bean
    ApplicationRunner crearTopicosBase(KafkaAdminService servicio,
                                       @Value("${kafka-admin.crear-topicos-al-iniciar}") boolean crear) {
        return args -> {
            if (!crear) return;
            try {
                List<String> creados = servicio.crearTopicosBase();
                log.info("Tópicos base creados: {}", creados);
            } catch (Exception e) {
                log.warn("No se pudieron crear los tópicos base: {}", e.getMessage());
            }
        };
    }
}

