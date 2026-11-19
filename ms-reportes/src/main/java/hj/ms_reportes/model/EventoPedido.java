package hj.ms_reportes.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Evento que publica ms-pedidos en orders.events. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EventoPedido(
        String eventId,
        String type,
        String timestamp,
        Long pedidoId,
        String usuario,
        String estadoNuevo,
        Double montoTotal,
        List<ItemEvento> items
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItemEvento(Long productoId, Integer cantidad, String nombreProducto, Integer subtotal) {}
}

