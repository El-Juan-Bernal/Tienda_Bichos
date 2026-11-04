package hj.ms_catalogo.dto;

import java.util.List;

/** Mensaje que publica ms-pedidos en q.cmd.stock. */
public record StockEventoDto(
        String tipo,
        Long pedidoId,
        List<ItemStock> items
) {
    public record ItemStock(Long productoId, Integer cantidad) {}
}

