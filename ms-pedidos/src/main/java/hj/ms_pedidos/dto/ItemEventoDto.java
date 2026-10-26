package hj.ms_pedidos.dto;

public record ItemEventoDto(
        Long productoId,
        Integer cantidad,
        String nombreProducto,
        Integer precioUnitario,
        Integer subtotal
) {}

