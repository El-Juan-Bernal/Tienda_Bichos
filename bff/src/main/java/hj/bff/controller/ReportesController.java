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

// Solo lectura. Solo Admin (lo define SecurityConfig). Reenvía a ms-reportes.
@RestController
@RequestMapping("/api/report")
@Tag(name = "Reportes (Admin)", description = "KPIs y productos más vendidos. Solo rol Admin.")
@ApiResponse(responseCode = "401", description = "Falta el token o no es válido")
@ApiResponse(responseCode = "403", description = "La cuenta no tiene el rol Admin")
public class ReportesController {

  private static final String JSON = MediaType.APPLICATION_JSON_VALUE;
  private static final String DESC_RANGO =
      "Rango de tiempo: lastNh (horas) o lastNd (días), por ejemplo last24h o last7d";

  private final Reenviador reenviador;

  public ReportesController(
      RestClient.Builder restClientBuilder,
      @Value("${microservices.reportes.base-url}") String baseUrl) {
    this.reenviador = new Reenviador(restClientBuilder, baseUrl, "El servicio de reportes");
  }

  @Operation(summary = "KPIs del período",
      description = "Requiere rol Admin. Pedidos vendidos, ventas totales, ticket promedio, lead time, "
          + "pedidos entregados y activos, pedidos por estado y ventas por hora (hora de Chile).",
      parameters = @Parameter(name = "range", in = ParameterIn.QUERY, description = DESC_RANGO,
          example = "last24h"))
  @ApiResponse(responseCode = "200", description = "KPIs del período",
      content = @Content(mediaType = JSON, schema = @Schema(type = "object")))
  @GetMapping("/kpis")
  public ResponseEntity<byte[]> kpis(HttpServletRequest request) {
    return reenviador.reenviar(request, null);
  }

  @Operation(summary = "Productos más vendidos",
      description = "Requiere rol Admin. Unidades e ingresos brutos (antes de descuentos); "
          + "las cancelaciones se descuentan.",
      parameters = {
        @Parameter(name = "range", in = ParameterIn.QUERY, description = DESC_RANGO, example = "last7d"),
        @Parameter(name = "limit", in = ParameterIn.QUERY, description = "Cantidad máxima de productos",
            example = "10")
      })
  @ApiResponse(responseCode = "200", description = "Lista de productos, de mayor a menor venta",
      content = @Content(mediaType = JSON, schema = @Schema(type = "array")))
  @GetMapping("/top-products")
  public ResponseEntity<byte[]> topProductos(HttpServletRequest request) {
    return reenviador.reenviar(request, null);
  }
}
