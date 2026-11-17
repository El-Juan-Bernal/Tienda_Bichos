package hj.ms_auditoria.dto;

import hj.ms_auditoria.entity.EventoAuditoria;
import java.time.Instant;

public record EventoAuditoriaResponse(
        String eventId,
        Long pedidoId,
        String tipo,
        String usuario,
        String origen,
        String estadoAnterior,
        String estadoNuevo,
        Double montoTotal,
        Instant ocurridoEn,
        String kafkaTopico,
        Integer kafkaParticion,
        Long kafkaOffset
) {
    public static EventoAuditoriaResponse de(EventoAuditoria e) {
        return new EventoAuditoriaResponse(
                e.getEventId(), e.getPedidoId(), e.getTipo(), e.getUsuario(), e.getOrigen(),
                e.getEstadoAnterior(), e.getEstadoNuevo(), e.getMontoTotal(), e.getOcurridoEn(),
                e.getKafkaTopico(), e.getKafkaParticion(), e.getKafkaOffset());
    }
}

