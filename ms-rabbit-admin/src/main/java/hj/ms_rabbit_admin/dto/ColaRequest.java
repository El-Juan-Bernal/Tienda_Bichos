package hj.ms_rabbit_admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record ColaRequest(
        @Schema(description = "Nombre de la cola", example = "q.cmd.demo")
        @NotBlank(message = "es obligatorio") String nombre,
        @Schema(description = "Sobrevive al reinicio del broker (por defecto true)", example = "true")
        Boolean durable,
        @Schema(description = "Se borra sola al quedar sin consumidores (por defecto false)", example = "false")
        Boolean autoDelete,
        @Schema(description = "Argumentos de RabbitMQ. Por ejemplo x-dead-letter-exchange y "
            + "x-dead-letter-routing-key para asociarle una cola de cartas muertas (DLQ)")
        Map<String, Object> argumentos) {
}
