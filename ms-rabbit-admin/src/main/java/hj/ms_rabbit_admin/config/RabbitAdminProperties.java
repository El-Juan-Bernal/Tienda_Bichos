package hj.ms_rabbit_admin.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Lo que se escribe en application.yaml bajo "rabbit-admin". */
@ConfigurationProperties(prefix = "rabbit-admin")
public record RabbitAdminProperties(@DefaultValue("/") String vhost, Map<String, Broker> brokers) {

    public RabbitAdminProperties {
        brokers = brokers == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(brokers));
    }

    /** Un broker: dirección de su API de administración y credenciales. */
    public record Broker(String url, String usuario, String clave) {
    }
}
