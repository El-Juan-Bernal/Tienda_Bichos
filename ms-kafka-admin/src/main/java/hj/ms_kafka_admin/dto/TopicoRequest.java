package hj.ms_kafka_admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.Map;

public record TopicoRequest(
    @NotBlank @Pattern(regexp = "[a-zA-Z0-9._-]+", message = "Solo letras, números, punto, guion y guion bajo")
    String nombre,
    @Min(1) int particiones,
    @Min(1) int replicas,
    Map<String, String> configuraciones
) {}

