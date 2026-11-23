package hj.ms_kafka_admin.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Lo que se escribe en application.yaml bajo "kafka-admin". */
@ConfigurationProperties(prefix = "kafka-admin")
public record KafkaAdminProperties(
        @DefaultValue("true") boolean crearTopicosAlIniciar,
        List<TopicoBase> topicosBase) {

    public KafkaAdminProperties {
        topicosBase = topicosBase == null ? List.of() : List.copyOf(topicosBase);
    }

    /** Un tópico que se crea al iniciar si falta. La retención va en días. */
    public record TopicoBase(String nombre, int particiones, int replicas, String politica, int retencionDias) {

        public TopicoBase {
            if (nombre == null || nombre.isBlank()) {
                throw new IllegalArgumentException("Cada tópico base necesita un nombre.");
            }
            if (particiones < 1 || replicas < 1 || retencionDias < 1) {
                throw new IllegalArgumentException(
                    "El tópico '" + nombre + "' debe tener particiones, réplicas y días de retención mayores a 0.");
            }
            if (politica == null || politica.isBlank()) {
                politica = "delete";
            }
        }
    }
}

