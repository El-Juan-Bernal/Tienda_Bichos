package hj.ms_reportes.model;

import java.util.List;

/** Último estado conocido de un pedido, armado a partir de sus eventos. */
public record PedidoEstado(
        Long pedidoId,
        String estado,
        String creadoEn,
        String aceptadoEn,
        String entregadoEn,
        Double monto,
        List<String> eventIds
) {}

