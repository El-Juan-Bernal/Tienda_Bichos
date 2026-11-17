package hj.ms_auditoria.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErroresConfig {

    /**
     * Si un mensaje falla, se reintenta 3 veces (1 s entre intentos) y luego se envía
     * a orders.events.audit.DLT, a la misma partición, con los datos del error en las cabeceras.
     */
    @Bean
    DefaultErrorHandler manejadorDeErrores(KafkaTemplate<String, String> plantilla,
                                           @Value("${kafka.topic.dlt}") String topicoDlt) {
        DeadLetterPublishingRecoverer recuperador = new DeadLetterPublishingRecoverer(
                plantilla, (registro, error) -> new TopicPartition(topicoDlt, registro.partition()));
        return new DefaultErrorHandler(recuperador, new FixedBackOff(1000L, 3L));
    }
}

