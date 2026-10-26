package hj.ms_pedidos.dto;

import java.util.List;

public record CrearPedidoRequest(
        String usuario,
        String pasarelaSeleccionada,
        Double montoTotal,
        List<ItemPedidoDto> items
) {}

