package hj.ms_reportes.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record KpisResponse(
        String rango,
        Instant desde,
        long pedidosVendidos,
        double ventasTotales,
        double ticketPromedio,
        long pedidosEntregados,
        Double leadTimePromedioSegundos,
        long pedidosActivos,
        Map<String, Long> pedidosPorEstado,
        List<VentaHora> ventasPorHora
) {}

