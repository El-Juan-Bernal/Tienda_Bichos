package hj.ms_rabbit_admin.dto;

import java.util.Map;

public record ColaResponse(
        String nombre,
        boolean durable,
        boolean autoDelete,
        Map<String, Object> argumentos,
        long mensajes,
        long listos,
        long consumidores,
        String estado) {
}
