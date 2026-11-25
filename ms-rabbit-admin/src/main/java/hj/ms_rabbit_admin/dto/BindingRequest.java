package hj.ms_rabbit_admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record BindingRequest(
        @Schema(description = "Exchange de origen", example = "cmd.direct")
        @NotBlank(message = "es obligatorio") String exchange,
        @Schema(description = "Cola de destino", example = "q.cmd.demo")
        @NotBlank(message = "es obligatorio") String cola,
        @Schema(description = "Routing key o patrón (en exchanges topic: * una palabra, # cero o más)", example = "demo.send")
        String routingKey) {
}
