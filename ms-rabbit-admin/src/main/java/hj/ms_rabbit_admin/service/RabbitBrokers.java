package hj.ms_rabbit_admin.service;

import hj.ms_rabbit_admin.config.RabbitAdminProperties;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

/** Un cliente HTTP ya autenticado por cada broker configurado en application.yaml. */
@Component
public class RabbitBrokers {

    /** Un broker configurado: su nombre, su dirección y el cliente HTTP listo para usar. */
    public record Cliente(String nombre, String url, RestClient http) {
    }

    private final Map<String, Cliente> clientes = new LinkedHashMap<>();

    public RabbitBrokers(RabbitAdminProperties propiedades, RestClient.Builder builder) {
        // HTTP/1.1 explícito: la API de RabbitMQ no necesita más y así se evita la negociación a HTTP/2
        HttpClient http11 = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(5))
            .build();
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(http11);
        fabrica.setReadTimeout(Duration.ofSeconds(10));

        propiedades.brokers().forEach((nombre, b) -> clientes.put(nombre, new Cliente(
            nombre,
            quitarBarraFinal(b.url()),
            builder.clone()
                .requestFactory(fabrica)
                .defaultHeaders(h -> h.setBasicAuth(b.usuario(), b.clave()))
                .build())));
    }

    public Cliente de(String nombre) {
        Cliente cliente = clientes.get(nombre);
        if (cliente == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Broker desconocido. Los disponibles son: " + String.join(", ", clientes.keySet()) + ".");
        }
        return cliente;
    }

    public Collection<Cliente> todos() {
        return clientes.values();
    }

    private static String quitarBarraFinal(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
