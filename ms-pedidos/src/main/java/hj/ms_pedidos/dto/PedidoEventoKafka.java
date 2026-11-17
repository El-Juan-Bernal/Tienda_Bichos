package hj.ms_pedidos.dto;

import hj.ms_pedidos.entity.EstadoPedido;
import hj.ms_pedidos.entity.Pedido;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Evento de negocio publicado en el tópico orders.events (key = pedidoId). */
public record PedidoEventoKafka(
        String eventId,
        String type,
        String timestamp,
        String traceId,
        String correlationId,
        Long pedidoId,
        String usuario,
        String origen,
        EstadoPedido estadoAnterior,
        EstadoPedido estadoNuevo,
        Double montoTotal,
        Double montoReembolsado,
        String pasarela,
        List<ItemEventoDto> items
) {
    public static PedidoEventoKafka de(Pedido pedido, EstadoPedido anterior, String origen,
                                       List<ItemEventoDto> items) {
        return new PedidoEventoKafka(
                UUID.randomUUID().toString(),
                tipoDe(pedido.getEstado()),
                Instant.now().toString(),
                UUID.randomUUID().toString(),
                "pedido-" + pedido.getId(),
                pedido.getId(),
                pedido.getUsuarioEmail(),
                origen,
                anterior,
                pedido.getEstado(),
                pedido.getMontoTotal(),
                pedido.getMontoReembolsado(),
                pedido.getPasarelaSeleccionada(),
                items);
    }

    private static String tipoDe(EstadoPedido estado) {
        return switch (estado) {
            case CREADO -> "OrderCreated";
            case ACEPTADO -> "OrderAccepted";
            case EN_PREPARACION -> "OrderPreparing";
            case DESPACHADO -> "OrderDispatched";
            case ENTREGADO -> "OrderDelivered";
            case CANCELADO -> "OrderCancelled";
        };
    }
}

