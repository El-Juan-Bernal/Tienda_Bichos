package hj.ms_pedidos.dto;

import hj.ms_pedidos.entity.EstadoPedido;

public record ActualizarEstadoRequest(
        EstadoPedido nuevoEstado
) {}

