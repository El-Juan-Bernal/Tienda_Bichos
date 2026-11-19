package hj.ms_reportes.topology;

import hj.ms_reportes.model.Acumulado;
import hj.ms_reportes.model.EventoPedido;
import hj.ms_reportes.model.PedidoEstado;
import hj.ms_reportes.util.Claves;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KGroupedTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableKafkaStreams
public class ReportesTopology {

    public static final String STORE_PEDIDOS = "pedidos-estado";
    public static final String STORE_ESTADOS = "estados-pedidos";
    public static final String STORE_VENTAS = "ventas-por-hora";
    public static final String STORE_LEAD_TIME = "lead-time-por-dia";
    public static final String STORE_TOP_PRODUCTOS = "top-productos-por-dia";

    private static final Logger log = LoggerFactory.getLogger(ReportesTopology.class);
    private static final String CANCELADO = "CANCELADO";

    private final JsonMapper json;

    public ReportesTopology(JsonMapper json) {
        this.json = json;
    }

    @Bean
    public KStream<String, String> topologiaReportes(
            StreamsBuilder builder,
            @Value("${kafka.topic.orders-events}") String topicoEventos,
            @Value("${kafka.topic.dlt}") String topicoDlt) {

        KStream<String, String> entrada =
                builder.stream(topicoEventos, Consumed.with(Serdes.String(), Serdes.String()));

        // Lo que no se puede interpretar va a la DLT de este consumidor y no detiene el stream
        entrada.filterNot(this::esValido)
                .peek((k, v) -> log.warn("Mensaje inválido enviado a la DLT (key={})", k))
                .to(topicoDlt, Produced.with(Serdes.String(), Serdes.String()));

        KStream<String, String> validos = entrada.filter(this::esValido);

        // 1) Último estado de cada pedido (key = pedidoId)
        KTable<String, String> pedidos = validos
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                .aggregate(() -> "", (k, evento, actual) -> aplicarEvento(evento, actual),
                        almacen(STORE_PEDIDOS));

        // 2) Pedidos por estado (se restan del estado anterior y se suman al nuevo)
        pedidos.groupBy((k, v) -> KeyValue.pair(estadoDe(v), v), Grouped.with(Serdes.String(), Serdes.String()))
                .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as(STORE_ESTADOS)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.Long()));

        // 3) Ventas por hora: pedidos aceptados que no fueron cancelados
        acumular(pedidos.groupBy((k, v) -> KeyValue.pair(claveVentas(v), v),
                        Grouped.with(Serdes.String(), Serdes.String())),
                STORE_VENTAS, this::contribucionVentas);

        // 4) Lead time por día de entrega (CREADO -> ENTREGADO)
        acumular(pedidos.groupBy((k, v) -> KeyValue.pair(claveLeadTime(v), v),
                        Grouped.with(Serdes.String(), Serdes.String())),
                STORE_LEAD_TIME, this::contribucionLeadTime);

        // 5) Top de productos por día: suma al aceptar y resta al cancelar
        validos.flatMap((k, v) -> deltasTopProductos(v))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                .aggregate(() -> escribir(Acumulado.vacio()),
                        (k, delta, actual) -> escribir(leerAcumulado(actual).sumar(leerAcumulado(delta))),
                        almacen(STORE_TOP_PRODUCTOS));

        return entrada;
    }

    private Materialized<String, String, KeyValueStore<Bytes, byte[]>> almacen(String nombre) {
        return Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as(nombre)
                .withKeySerde(Serdes.String())
                .withValueSerde(Serdes.String());
    }

    private void acumular(KGroupedTable<String, String> agrupado, String store,
                          Function<String, Acumulado> contribucion) {
        agrupado.aggregate(
                () -> escribir(Acumulado.vacio()),
                (k, pedido, acc) -> escribir(leerAcumulado(acc).sumar(contribucion.apply(pedido))),
                (k, pedido, acc) -> escribir(leerAcumulado(acc).restar(contribucion.apply(pedido))),
                almacen(store));
    }

    // ---------- Validación y estado del pedido ----------

    private boolean esValido(String clave, String valor) {
        if (clave == null || valor == null) {
            return false;
        }
        try {
            EventoPedido e = json.readValue(valor, EventoPedido.class);
            Instant.parse(e.timestamp());
            return e.eventId() != null && e.type() != null && e.pedidoId() != null;
        } catch (Exception _) {
            return false;
        }
    }

    private String aplicarEvento(String valor, String actual) {
        EventoPedido e = json.readValue(valor, EventoPedido.class);
        PedidoEstado p = vacio(actual) ? null : json.readValue(actual, PedidoEstado.class);

        List<String> ids = p == null ? new ArrayList<>() : new ArrayList<>(p.eventIds());
        if (ids.contains(e.eventId())) {
            return actual; // evento repetido: se ignora
        }
        ids.add(e.eventId());

        String creado = p == null ? null : p.creadoEn();
        String aceptado = p == null ? null : p.aceptadoEn();
        String entregado = p == null ? null : p.entregadoEn();
        switch (e.type()) {
            case "OrderCreated" -> creado = e.timestamp();
            case "OrderAccepted" -> aceptado = e.timestamp();
            case "OrderDelivered" -> entregado = e.timestamp();
            default -> { /* los demás eventos solo cambian el estado */ }
        }

        String estado = valorOActual(e.estadoNuevo(), p == null ? null : p.estado());
        Double monto = valorOActual(e.montoTotal(), p == null ? null : p.monto());
        return escribir(new PedidoEstado(e.pedidoId(), estado, creado, aceptado, entregado, monto, ids));
    }

    /** Devuelve el valor nuevo si existe; si no, el que ya había. */
    private <T> T valorOActual(T nuevo, T actual) {
        return nuevo != null ? nuevo : actual;
    }

    private boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }

    // ---------- Claves y contribuciones por KPI ----------

    private String estadoDe(String pedido) {
        if (vacio(pedido)) {
            return Claves.OMITIDO;
        }
        String estado = leerPedido(pedido).estado();
        return estado != null ? estado : Claves.OMITIDO;
    }

    private String claveVentas(String pedido) {
        if (vacio(pedido)) {
            return Claves.OMITIDO;
        }
        PedidoEstado p = leerPedido(pedido);
        if (p.aceptadoEn() == null || CANCELADO.equals(p.estado())) {
            return Claves.OMITIDO;
        }
        return Claves.hora(Instant.parse(p.aceptadoEn()));
    }

    private Acumulado contribucionVentas(String pedido) {
        if (vacio(pedido) || Claves.OMITIDO.equals(claveVentas(pedido))) {
            return Acumulado.vacio();
        }
        Double monto = leerPedido(pedido).monto();
        return new Acumulado(null, 1, monto != null ? monto : 0, 0);
    }

    private String claveLeadTime(String pedido) {
        if (vacio(pedido)) {
            return Claves.OMITIDO;
        }
        PedidoEstado p = leerPedido(pedido);
        if (p.creadoEn() == null || p.entregadoEn() == null) {
            return Claves.OMITIDO;
        }
        return Claves.dia(Instant.parse(p.entregadoEn()));
    }

    private Acumulado contribucionLeadTime(String pedido) {
        if (vacio(pedido) || Claves.OMITIDO.equals(claveLeadTime(pedido))) {
            return Acumulado.vacio();
        }
        PedidoEstado p = leerPedido(pedido);
        long segundos = Math.max(0,
                Duration.between(Instant.parse(p.creadoEn()), Instant.parse(p.entregadoEn())).toSeconds());
        return new Acumulado(null, 1, 0, (double) segundos);
    }

    private List<KeyValue<String, String>> deltasTopProductos(String valor) {
        EventoPedido e = json.readValue(valor, EventoPedido.class);
        int signo = switch (e.type()) {
            case "OrderAccepted" -> 1;
            case "OrderCancelled" -> -1;
            default -> 0;
        };
        List<KeyValue<String, String>> deltas = new ArrayList<>();
        if (signo == 0 || e.items() == null) {
            return deltas;
        }
        String dia = Claves.dia(Instant.parse(e.timestamp()));
        for (EventoPedido.ItemEvento item : e.items()) {
            if (item.productoId() == null || item.cantidad() == null) {
                continue;
            }
            double ingresos = item.subtotal() != null ? item.subtotal() : 0;
            deltas.add(KeyValue.pair(dia + "|" + item.productoId(),
                    escribir(new Acumulado(item.nombreProducto(), (long) signo * item.cantidad(),
                            signo * ingresos, 0))));
        }
        return deltas;
    }

    // ---------- JSON ----------

    private PedidoEstado leerPedido(String valor) {
        return json.readValue(valor, PedidoEstado.class);
    }

    private Acumulado leerAcumulado(String valor) {
        return json.readValue(valor, Acumulado.class);
    }

    private String escribir(Object objeto) {
        return json.writeValueAsString(objeto);
    }
}

