package hj.ms_reportes.service;

import static hj.ms_reportes.topology.ReportesTopology.STORE_ESTADOS;
import static hj.ms_reportes.topology.ReportesTopology.STORE_LEAD_TIME;
import static hj.ms_reportes.topology.ReportesTopology.STORE_TOP_PRODUCTOS;
import static hj.ms_reportes.topology.ReportesTopology.STORE_VENTAS;

import hj.ms_reportes.dto.KpisResponse;
import hj.ms_reportes.dto.TopProducto;
import hj.ms_reportes.dto.VentaHora;
import hj.ms_reportes.model.Acumulado;
import hj.ms_reportes.util.Claves;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

/** Consultas de solo lectura sobre los state stores (interactive queries). */
@Service
public class ReportesService {

    private static final Pattern RANGO = Pattern.compile("last(\\d{1,3})([hd])");
    private static final Set<String> ESTADOS_ACTIVOS = Set.of("ACEPTADO", "EN_PREPARACION", "DESPACHADO");

    private final StreamsBuilderFactoryBean fabrica;
    private final JsonMapper json;

    public ReportesService(StreamsBuilderFactoryBean fabrica, JsonMapper json) {
        this.fabrica = fabrica;
        this.json = json;
    }

    public KpisResponse kpis(String rango) {
        Instant desde = desdeDe(rango);
        String corteHora = Claves.hora(desde);
        String corteDia = Claves.dia(desde);

        List<VentaHora> horas = new ArrayList<>();
        long pedidos = 0;
        double ventas = 0;
        ReadOnlyKeyValueStore<String, String> storeVentas = store(STORE_VENTAS);
        try (KeyValueIterator<String, String> it = storeVentas.all()) {
            while (it.hasNext()) {
                KeyValue<String, String> kv = it.next();
                Acumulado a = leer(kv.value);
                if (Claves.OMITIDO.equals(kv.key) || kv.key.compareTo(corteHora) < 0 || a.cantidad() <= 0) {
                    continue;
                }
                horas.add(new VentaHora(kv.key, a.cantidad(), a.monto()));
                pedidos += a.cantidad();
                ventas += a.monto();
            }
        }
        horas.sort(Comparator.comparing(VentaHora::hora));

        long entregados = 0;
        double segundos = 0;
        ReadOnlyKeyValueStore<String, String> storeLead = store(STORE_LEAD_TIME);
        try (KeyValueIterator<String, String> it = storeLead.all()) {
            while (it.hasNext()) {
                KeyValue<String, String> kv = it.next();
                Acumulado a = leer(kv.value);
                if (Claves.OMITIDO.equals(kv.key) || kv.key.compareTo(corteDia) < 0 || a.cantidad() <= 0) {
                    continue;
                }
                entregados += a.cantidad();
                segundos += a.segundos();
            }
        }

        Map<String, Long> estados = new TreeMap<>();
        long activos = 0;
        ReadOnlyKeyValueStore<String, Long> storeEstados = store(STORE_ESTADOS);
        try (KeyValueIterator<String, Long> it = storeEstados.all()) {
            while (it.hasNext()) {
                KeyValue<String, Long> kv = it.next();
                if (Claves.OMITIDO.equals(kv.key) || kv.value <= 0) {
                    continue;
                }
                estados.put(kv.key, kv.value);
                if (ESTADOS_ACTIVOS.contains(kv.key)) {
                    activos += kv.value;
                }
            }
        }

        return new KpisResponse(
                rango, desde, pedidos, ventas,
                pedidos == 0 ? 0 : ventas / pedidos,
                entregados,
                entregados == 0 ? null : segundos / entregados,
                activos, estados, horas);
    }

    public List<TopProducto> topProductos(String rango, int limite) {
        String corteDia = Claves.dia(desdeDe(rango));
        Map<Long, Acumulado> porProducto = new HashMap<>();

        ReadOnlyKeyValueStore<String, String> store = store(STORE_TOP_PRODUCTOS);
        try (KeyValueIterator<String, String> it = store.all()) {
            while (it.hasNext()) {
                KeyValue<String, String> kv = it.next();
                String[] partes = kv.key.split("\\|");
                if (partes.length != 2 || partes[0].compareTo(corteDia) < 0) {
                    continue;
                }
                porProducto.merge(Long.valueOf(partes[1]), leer(kv.value), Acumulado::sumar);
            }
        }

        return porProducto.entrySet().stream()
                .filter(e -> e.getValue().cantidad() > 0)
                .map(e -> new TopProducto(e.getKey(), e.getValue().etiqueta(),
                        e.getValue().cantidad(), e.getValue().monto()))
                .sorted(Comparator.comparingLong(TopProducto::unidades).reversed()
                        .thenComparing(Comparator.comparingDouble(TopProducto::ingresos).reversed()))
                .limit(Math.clamp(limite, 1, 50))
                .toList();
    }

    // ---------- Utilidades ----------

    /** Acepta lastNh o lastNd (ej.: last24h, last7d). */
    private Instant desdeDe(String rango) {
        Matcher m = RANGO.matcher(rango == null ? "" : rango.trim().toLowerCase());
        if (!m.matches() || Integer.parseInt(m.group(1)) == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Rango no válido. Usa por ejemplo last24h, last72h o last7d.");
        }
        int cantidad = Integer.parseInt(m.group(1));
        Duration duracion = "h".equals(m.group(2)) ? Duration.ofHours(cantidad) : Duration.ofDays(cantidad);
        return Instant.now().minus(duracion);
    }

    private <V> ReadOnlyKeyValueStore<String, V> store(String nombre) {
        KafkaStreams streams = fabrica.getKafkaStreams();
        if (streams == null || streams.state() != KafkaStreams.State.RUNNING) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Los reportes se están inicializando. Intenta de nuevo en unos segundos.");
        }
        try {
            return streams.store(StoreQueryParameters.fromNameAndType(
                    nombre, QueryableStoreTypes.<String, V>keyValueStore()));
        } catch (InvalidStateStoreException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El almacén de datos aún no está disponible. Intenta de nuevo en unos segundos.");
        }
    }

    private Acumulado leer(String valor) {
        return json.readValue(valor, Acumulado.class);
    }
}

