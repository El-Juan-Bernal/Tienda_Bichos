package hj.bff.controller;

import hj.bff.service.Reenviador;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

// Solo lectura. Solo Admin (lo define SecurityConfig). Reenvía a ms-auditoria.
@RestController
@RequestMapping("/api/audit")
@Tag(name = "Auditoría (Admin)", description = "Trazabilidad de pedidos (solo lectura). Solo rol Admin.")
@ApiResponse(responseCode = "401", description = "Falta el token o no es válido")
@ApiResponse(responseCode = "403", description = "La cuenta no tiene el rol Admin")
public class AuditoriaController {

  private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

  private final Reenviador reenviador;

  public AuditoriaController(
      RestClient.Builder restClientBuilder,
      @Value("${microservices.auditoria.base-url}") String baseUrl) {
    this.reenviador = new Reenviador(restClientBuilder, baseUrl, "El servicio de auditoría");
  }

  @Operation(summary = "Línea de tiempo de un pedido",
      description = "Requiere rol Admin. Todos los eventos del pedido, desde que se crea.",
      parameters = @Parameter(name = "pedidoId", in = ParameterIn.PATH, required = true,
          description = "Id del pedido", example = "63"))
  @ApiResponse(responseCode = "200", description = "Eventos del pedido",
      content = @Content(mediaType = JSON, schema = @Schema(type = "array")))
  @GetMapping("/orders/{pedidoId}/timeline")
  public ResponseEntity<byte[]> timeline(HttpServletRequest request) {
    return reenviador.reenviar(request, null);
  }

  @Operation(summary = "Buscar eventos",
      description = "Requiere rol Admin. Lista paginada, del más reciente al más antiguo. "
          + "Todos los filtros son opcionales.",
      parameters = {
        @Parameter(name = "usuario", in = ParameterIn.QUERY, description = "Correo del usuario"),
        @Parameter(name = "tipo", in = ParameterIn.QUERY,
            description = "OrderCreated, OrderAccepted, OrderPreparing, OrderDispatched, "
                + "OrderDelivered u OrderCancelled"),
        @Parameter(name = "desde", in = ParameterIn.QUERY, description = "Instante ISO en UTC",
            example = "2026-11-16T00:00:00Z"),
        @Parameter(name = "hasta", in = ParameterIn.QUERY, description = "Instante ISO en UTC",
            example = "2026-11-18T23:59:59Z"),
        @Parameter(name = "pagina", in = ParameterIn.QUERY, description = "Número de página, desde 0",
            example = "0"),
        @Parameter(name = "tamano", in = ParameterIn.QUERY, description = "Eventos por página (máximo 100)",
            example = "20")
      })
  @ApiResponse(responseCode = "200", description = "Página de eventos",
      content = @Content(mediaType = JSON, schema = @Schema(type = "object")))
  @GetMapping("/events")
  public ResponseEntity<byte[]> eventos(HttpServletRequest request) {
    return reenviador.reenviar(request, null);
  }
}
