package hj.ms_auditoria.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Evento que publica ms-pedidos en orders.events. Los campos que no se usan se ignoran. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PedidoEventoRecibido(
        String eventId,
        String type,
        String timestamp,
        String traceId,
        Long pedidoId,
        String usuario,
        String origen,
        String estadoAnterior,
        String estadoNuevo,
        Double montoTotal
) {}

