package hj.ms_auditoria.service;

import hj.ms_auditoria.dto.EntradaTimeline;
import hj.ms_auditoria.dto.EventoAuditoriaResponse;
import hj.ms_auditoria.dto.PaginaResponse;
import hj.ms_auditoria.dto.PedidoEventoRecibido;
import hj.ms_auditoria.entity.EventoAuditoria;
import hj.ms_auditoria.repository.EventoAuditoriaRepository;
import java.time.Instant;
import java.util.List;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);
    private static final int TAMANO_MAXIMO = 100;

    private final EventoAuditoriaRepository repositorio;
    private final KafkaTemplate<String, String> kafka;
    private final JsonMapper json;
    private final String topicoTimeline;

    public AuditoriaService(EventoAuditoriaRepository repositorio,
                            KafkaTemplate<String, String> kafka,
                            JsonMapper json,
                            @Value("${kafka.topic.audit-timeline}") String topicoTimeline) {
        this.repositorio = repositorio;
        this.kafka = kafka;
        this.json = json;
        this.topicoTimeline = topicoTimeline;
    }

    /** Guarda el evento (si no existe) y republica la entrada en audit.timeline. */
    public void registrar(ConsumerRecord<String, String> registro) {
        PedidoEventoRecibido evento = json.readValue(registro.value(), PedidoEventoRecibido.class);
        if (evento.eventId() == null || evento.pedidoId() == null || evento.timestamp() == null) {
            throw new IllegalArgumentException("Evento sin eventId, pedidoId o timestamp");
        }
        if (repositorio.existsByEventId(evento.eventId())) {
            log.info("Evento {} ya registrado; se ignora el duplicado.", evento.eventId());
            return;
        }

        EventoAuditoria guardado;
        try {
            guardado = repositorio.save(EventoAuditoria.desde(evento, registro));
        } catch (DataIntegrityViolationException _) {
            log.info("Evento {} ya registrado (restricción única); se ignora.", evento.eventId());
            return;
        }

        log.info("Auditoría: {} del pedido #{} ({}-{} offset {})", guardado.getTipo(), guardado.getPedidoId(),
                registro.topic(), registro.partition(), registro.offset());
        publicarTimeline(guardado);
    }

    public List<EventoAuditoriaResponse> timelineDePedido(Long pedidoId) {
        return repositorio.findByPedidoIdOrderByOcurridoEnAscIdAsc(pedidoId).stream()
                .map(EventoAuditoriaResponse::de)
                .toList();
    }

        public PaginaResponse<EventoAuditoriaResponse> buscar(String usuario, String tipo, Instant desde,
                                                          Instant hasta, int pagina, int tamano) {
        int size = Math.clamp(tamano, 1, TAMANO_MAXIMO);
        Page<EventoAuditoria> resultado = repositorio.buscar(
                vacioANulo(usuario), vacioANulo(tipo), desde, hasta,
                PageRequest.of(Math.clamp(pagina, 0, Integer.MAX_VALUE), size));
        return new PaginaResponse<>(
                resultado.getContent().stream().map(EventoAuditoriaResponse::de).toList(),
                resultado.getNumber(), resultado.getSize(), resultado.getTotalElements());
    }

    private void publicarTimeline(EventoAuditoria e) {
        EntradaTimeline entrada = new EntradaTimeline(
                e.getEventId(),
                e.getPedidoId(),
                e.getUsuario(),
                e.getOrigen(),
                e.getTipo(),
                e.getEstadoAnterior(),
                e.getEstadoNuevo(),
                e.getOcurridoEn().toString(),
                e.getKafkaTopico() + "-" + e.getKafkaParticion() + "@" + e.getKafkaOffset());
        try {
            kafka.send(topicoTimeline, String.valueOf(e.getPedidoId()), json.writeValueAsString(entrada))
                    .whenComplete((resultado, error) -> {
                        if (error != null) {
                            log.warn("No se pudo publicar la entrada del pedido #{} en {}: {}",
                                    e.getPedidoId(), topicoTimeline, error.getMessage());
                        }
                    });
        } catch (Exception ex) {
            log.warn("No se pudo publicar la entrada del pedido #{} en {}: {}",
                    e.getPedidoId(), topicoTimeline, ex.getMessage());
        }
    }

    private String vacioANulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}

