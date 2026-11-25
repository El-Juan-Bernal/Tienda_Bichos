package hj.ms_rabbit_admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ExchangeRequest(
        @Schema(description = "Nombre del exchange", example = "cmd.demo")
        @NotBlank(message = "es obligatorio") String nombre,
        @Schema(description = "Tipo de exchange", example = "direct", allowableValues = {"direct", "topic", "fanout", "headers"})
        @NotBlank(message = "es obligatorio")
        @Pattern(regexp = "direct|topic|fanout|headers", message = "debe ser direct, topic, fanout o headers") String tipo,
        @Schema(description = "Sobrevive al reinicio del broker (por defecto true)", example = "true")
        Boolean durable,
        @Schema(description = "Se borra solo al quedar sin bindings (por defecto false)", example = "false")
        Boolean autoDelete) {
}
