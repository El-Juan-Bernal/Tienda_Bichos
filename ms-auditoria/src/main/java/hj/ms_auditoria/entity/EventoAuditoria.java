package hj.ms_auditoria.entity;

import hj.ms_auditoria.dto.PedidoEventoRecibido;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@Entity
@Table(name = "auditoria_eventos", indexes = {
        @Index(name = "idx_auditoria_pedido", columnList = "pedido_id"),
        @Index(name = "idx_auditoria_usuario", columnList = "usuario"),
        @Index(name = "idx_auditoria_fecha", columnList = "ocurrido_en")
})
public class EventoAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Único: es lo que evita registrar dos veces el mismo evento
    @Column(name = "event_id", nullable = false, unique = true, length = 64)
    private String eventId;

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @Column(name = "tipo", nullable = false, length = 40)
    private String tipo;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    @Column(name = "usuario")
    private String usuario;

    @Column(name = "origen", length = 20)
    private String origen;

    @Column(name = "estado_anterior", length = 30)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", length = 30)
    private String estadoNuevo;

    @Column(name = "monto_total")
    private Double montoTotal;

    @Column(name = "ocurrido_en", nullable = false)
    private Instant ocurridoEn;

    @Column(name = "kafka_topico", length = 100)
    private String kafkaTopico;

    @Column(name = "kafka_particion")
    private Integer kafkaParticion;

    @Column(name = "kafka_offset")
    private Long kafkaOffset;

    @Column(name = "registrado_en", nullable = false)
    private Instant registradoEn;

    protected EventoAuditoria() {
    }

    public static EventoAuditoria desde(PedidoEventoRecibido e, ConsumerRecord<String, String> registro) {
        EventoAuditoria a = new EventoAuditoria();
        a.eventId = e.eventId();
        a.traceId = e.traceId();
        a.tipo = e.type();
        a.pedidoId = e.pedidoId();
        a.usuario = e.usuario();
        a.origen = e.origen();
        a.estadoAnterior = e.estadoAnterior();
        a.estadoNuevo = e.estadoNuevo();
        a.montoTotal = e.montoTotal();
        a.ocurridoEn = Instant.parse(e.timestamp());
        a.kafkaTopico = registro.topic();
        a.kafkaParticion = registro.partition();
        a.kafkaOffset = registro.offset();
        a.registradoEn = Instant.now();
        return a;
    }

    public Long getId() { return id; }
    public String getEventId() { return eventId; }
    public String getTraceId() { return traceId; }
    public String getTipo() { return tipo; }
    public Long getPedidoId() { return pedidoId; }
    public String getUsuario() { return usuario; }
    public String getOrigen() { return origen; }
    public String getEstadoAnterior() { return estadoAnterior; }
    public String getEstadoNuevo() { return estadoNuevo; }
    public Double getMontoTotal() { return montoTotal; }
    public Instant getOcurridoEn() { return ocurridoEn; }
    public String getKafkaTopico() { return kafkaTopico; }
    public Integer getKafkaParticion() { return kafkaParticion; }
    public Long getKafkaOffset() { return kafkaOffset; }
    public Instant getRegistradoEn() { return registradoEn; }
}

