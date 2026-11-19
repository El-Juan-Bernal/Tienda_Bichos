package hj.ms_reportes.config;

import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

/** Si un hilo de Streams falla por un error inesperado, se reemplaza en vez de dejar el servicio muerto. */
@Configuration
public class StreamsErroresConfig {

    private static final Logger log = LoggerFactory.getLogger(StreamsErroresConfig.class);

    public StreamsErroresConfig(StreamsBuilderFactoryBean fabrica) {
        fabrica.setStreamsUncaughtExceptionHandler(error -> {
            log.error("Error en un hilo de Kafka Streams; se reemplaza el hilo: {}", error.getMessage());
            return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD;
        });
    }
}

