package hj.ms_pedidos.dto;

import hj.ms_pedidos.entity.EstadoPedido;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoEventoDto(
        Long pedidoId,
        String usuarioEmail,
        EstadoPedido estado,
        LocalDateTime fechaEvento,
        List<ItemEventoDto> items,
        Integer subtotalNeto,
        Integer iva,
        Integer total
) {}

