package hj.ms_rabbit_admin.dto;

import java.util.Map;

public record ExchangeResponse(
        String nombre,
        String tipo,
        boolean durable,
        boolean autoDelete,
        boolean interno,
        Map<String, Object> argumentos) {
}
