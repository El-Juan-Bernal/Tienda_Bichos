package hj.ms_pedidos.dto;

import java.util.List;

/**
 * Evento hacia ms-catalogo por la cola q.cmd.stock.
 * tipo: REPOSICION_CANCELACION (devolver unidades al stock)
 *       PEDIDO_DESPACHADO (evaluar si corresponde reabastecer)
 */
public record StockEventoDto(
        String tipo,
        Long pedidoId,
        List<ItemStock> items
) {
    public record ItemStock(Long productoId, Integer cantidad) {}
}

