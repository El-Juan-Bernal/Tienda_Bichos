package hj.ms_rabbit_admin.service;

import hj.ms_rabbit_admin.config.RabbitAdminProperties;
import hj.ms_rabbit_admin.dto.BindingRequest;
import hj.ms_rabbit_admin.dto.BindingResponse;
import hj.ms_rabbit_admin.dto.BrokerResponse;
import hj.ms_rabbit_admin.dto.ColaRequest;
import hj.ms_rabbit_admin.dto.ColaResponse;
import hj.ms_rabbit_admin.dto.ExchangeRequest;
import hj.ms_rabbit_admin.dto.ExchangeResponse;
import hj.ms_rabbit_admin.service.RabbitBrokers.Cliente;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

/** Administra colas, exchanges y bindings usando la API HTTP de administración de RabbitMQ. */
@Service
public class RabbitAdminService {

    private static final ParameterizedTypeReference<List<Map<String, Object>>> LISTA =
        new ParameterizedTypeReference<List<Map<String, Object>>>() { };
    private static final ParameterizedTypeReference<Map<String, Object>> OBJETO =
        new ParameterizedTypeReference<Map<String, Object>>() { };

    private static final String DURABLE = "durable";
    private static final String AUTO_DELETE = "auto_delete";
    private static final String ARGUMENTS = "arguments";
    private static final String QUEUES = "queues";
    private static final String EXCHANGES = "exchanges";
    private static final String BINDINGS = "bindings";

    private final RabbitBrokers brokers;
    private final String vhost;

    public RabbitAdminService(RabbitBrokers brokers, RabbitAdminProperties propiedades) {
        this.brokers = brokers;
        this.vhost = propiedades.vhost();
    }

    // ------------------------------------------------------------------ brokers

    public List<BrokerResponse> listarBrokers() {
        return brokers.todos().stream().map(c -> new BrokerResponse(c.nombre(), c.url())).toList();
    }

    public Map<String, Object> resumen(String broker) {
        Cliente c = brokers.de(broker);
        Map<String, Object> datos = consultar(() -> c.http().get().uri(url(c, "overview")).retrieve().body(OBJETO));
        Map<String, Object> m = datos == null ? Map.of() : datos;
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("broker", c.nombre());
        resultado.put("url", c.url());
        resultado.put("version", texto(m, "rabbitmq_version"));
        resultado.put("cluster", texto(m, "cluster_name"));
        resultado.put("nodo", texto(m, "node"));
        return resultado;
    }

    // ------------------------------------------------------------------ colas

    public List<ColaResponse> listarColas(String broker) {
        Cliente c = brokers.de(broker);
        return lista(c, QUEUES, enc(vhost)).stream()
            .map(this::aCola)
            .sorted(Comparator.comparing(ColaResponse::nombre))
            .toList();
    }

    public void crearCola(String broker, ColaRequest r) {
        Cliente c = brokers.de(broker);
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put(DURABLE, r.durable() == null || r.durable());
        cuerpo.put(AUTO_DELETE, Boolean.TRUE.equals(r.autoDelete()));
        cuerpo.put(ARGUMENTS, r.argumentos() == null ? Map.of() : r.argumentos());
        ejecutar(() -> c.http().put()
            .uri(url(c, QUEUES, enc(vhost), enc(r.nombre())))
            .contentType(MediaType.APPLICATION_JSON)
            .body(cuerpo)
            .retrieve()
            .toBodilessEntity());
    }

    public void eliminarCola(String broker, String nombre) {
        Cliente c = brokers.de(broker);
        ejecutar(() -> c.http().delete().uri(url(c, QUEUES, enc(vhost), enc(nombre))).retrieve().toBodilessEntity());
    }

    // ------------------------------------------------------------------ exchanges

    public List<ExchangeResponse> listarExchanges(String broker, boolean incluirSistema) {
        Cliente c = brokers.de(broker);
        return lista(c, EXCHANGES, enc(vhost)).stream()
            .filter(m -> incluirSistema || !esDelSistema(texto(m, "name")))
            .map(this::aExchange)
            .sorted(Comparator.comparing(ExchangeResponse::nombre))
            .toList();
    }

    public void crearExchange(String broker, ExchangeRequest r) {
        Cliente c = brokers.de(broker);
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("type", r.tipo());
        cuerpo.put(DURABLE, r.durable() == null || r.durable());
        cuerpo.put(AUTO_DELETE, Boolean.TRUE.equals(r.autoDelete()));
        cuerpo.put("internal", false);
        cuerpo.put(ARGUMENTS, Map.of());
        ejecutar(() -> c.http().put()
            .uri(url(c, EXCHANGES, enc(vhost), enc(r.nombre())))
            .contentType(MediaType.APPLICATION_JSON)
            .body(cuerpo)
            .retrieve()
            .toBodilessEntity());
    }

    public void eliminarExchange(String broker, String nombre) {
        Cliente c = brokers.de(broker);
        ejecutar(() -> c.http().delete().uri(url(c, EXCHANGES, enc(vhost), enc(nombre))).retrieve().toBodilessEntity());
    }

    // ------------------------------------------------------------------ bindings

    public List<BindingResponse> listarBindings(String broker, boolean incluirSistema) {
        Cliente c = brokers.de(broker);
        return lista(c, BINDINGS, enc(vhost)).stream()
            .filter(m -> incluirSistema || !texto(m, "source").isEmpty())
            .map(this::aBinding)
            .sorted(Comparator.comparing(BindingResponse::exchange).thenComparing(BindingResponse::destino))
            .toList();
    }

    public void crearBinding(String broker, BindingRequest r) {
        Cliente c = brokers.de(broker);
        Map<String, Object> cuerpo = Map.of(
            "routing_key", r.routingKey() == null ? "" : r.routingKey(),
            ARGUMENTS, Map.of());
        ejecutar(() -> c.http().post()
            .uri(url(c, BINDINGS, enc(vhost), "e", enc(r.exchange()), "q", enc(r.cola())))
            .contentType(MediaType.APPLICATION_JSON)
            .body(cuerpo)
            .retrieve()
            .toBodilessEntity());
    }

    /** RabbitMQ identifica cada binding con su "properties_key": se busca primero y luego se borra. */
    public void eliminarBinding(String broker, String exchange, String cola, String routingKey) {
        Cliente c = brokers.de(broker);
        String clave = routingKey == null ? "" : routingKey;
        Map<String, Object> objetivo = lista(c, BINDINGS, enc(vhost), "e", enc(exchange), "q", enc(cola)).stream()
            .filter(m -> texto(m, "routing_key").equals(clave))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El binding no existe."));
        String propiedades = texto(objetivo, "properties_key");
        ejecutar(() -> c.http().delete()
            .uri(url(c, BINDINGS, enc(vhost), "e", enc(exchange), "q", enc(cola), enc(propiedades)))
            .retrieve()
            .toBodilessEntity());
    }

    // ------------------------------------------------------------------ llamadas y errores

    private List<Map<String, Object>> lista(Cliente c, String... partes) {
        List<Map<String, Object>> datos = consultar(() -> c.http().get().uri(url(c, partes)).retrieve().body(LISTA));
        return datos == null ? List.of() : datos;
    }

    private <T> T consultar(Supplier<T> llamada) {
        try {
            return llamada.get();
        } catch (RestClientResponseException e) {
            throw traducir(e);
        } catch (ResourceAccessException _) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo conectar con el broker.");
        }
    }

    private void ejecutar(Runnable llamada) {
        consultar(() -> {
            llamada.run();
            return null;
        });
    }

    private ResponseStatusException traducir(RestClientResponseException e) {
        int estado = e.getStatusCode().value();
        if (estado == 404) {
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe en el broker.");
        }
        if (estado == 401 || estado == 403) {
            return new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "El broker rechazó las credenciales configuradas (el usuario necesita el tag administrator).");
        }
        if (estado == 400) {
            return new ResponseStatusException(HttpStatus.BAD_REQUEST, razon(e));
        }
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "El broker respondió con el error " + estado + ".");
    }

    /** RabbitMQ explica el rechazo en el campo "reason" de su respuesta. */
    private String razon(RestClientResponseException e) {
        try {
            Map<?, ?> cuerpo = e.getResponseBodyAs(Map.class);
            Object razon = cuerpo == null ? null : cuerpo.get("reason");
            if (razon != null) {
                return razon.toString();
            }
        } catch (RuntimeException _) {
            // el cuerpo no era JSON: se usa el mensaje genérico
        }
        return "El broker rechazó la solicitud.";
    }

    // ------------------------------------------------------------------ conversiones

    private ColaResponse aCola(Map<String, Object> m) {
        return new ColaResponse(texto(m, "name"), bool(m, DURABLE), bool(m, AUTO_DELETE), mapa(m, ARGUMENTS),
            numero(m, "messages"), numero(m, "messages_ready"), numero(m, "consumers"), texto(m, "state"));
    }

    private ExchangeResponse aExchange(Map<String, Object> m) {
        return new ExchangeResponse(texto(m, "name"), texto(m, "type"), bool(m, DURABLE), bool(m, AUTO_DELETE),
            bool(m, "internal"), mapa(m, ARGUMENTS));
    }

    private BindingResponse aBinding(Map<String, Object> m) {
        return new BindingResponse(texto(m, "source"), texto(m, "destination"),
            texto(m, "destination_type"), texto(m, "routing_key"));
    }

    private static boolean esDelSistema(String nombre) {
        return nombre.isEmpty() || nombre.startsWith("amq.");
    }

    private static String texto(Map<String, Object> m, String clave) {
        Object valor = m.get(clave);
        return valor == null ? "" : valor.toString();
    }

    private static boolean bool(Map<String, Object> m, String clave) {
        return Boolean.TRUE.equals(m.get(clave));
    }

    private static long numero(Map<String, Object> m, String clave) {
        return m.get(clave) instanceof Number n ? n.longValue() : 0L;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapa(Map<String, Object> m, String clave) {
        return m.get(clave) instanceof Map<?, ?> valor ? (Map<String, Object>) valor : Map.of();
    }

    // ------------------------------------------------------------------ direcciones

    private static URI url(Cliente c, String... partes) {
        return URI.create(c.url() + "/api/" + String.join("/", partes));
    }

    /** Codifica un trozo de ruta ("/" pasa a %2F, que es como RabbitMQ pide el vhost por defecto). */
    private static String enc(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
