package hj.bff.controller;

import hj.bff.service.Reenviador;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

// Administración de RabbitMQ. Solo Admin (lo define SecurityConfig). Reenvía a ms-rabbit-admin.
@RestController
@RequestMapping("/api/rabbit-admin")
@Tag(name = "RabbitMQ admin (Admin)", description = "Colas, exchanges y bindings de los dos brokers. Solo rol Admin.")
@ApiResponse(responseCode = "401", description = "Falta el token o no es válido")
@ApiResponse(responseCode = "403", description = "La cuenta no tiene el rol Admin")
public class RabbitAdminController {

  private static final String JSON = MediaType.APPLICATION_JSON_VALUE;
  private static final String ADMIN = "Requiere rol Admin. ";
  private static final String DESC_BROKER = "Broker: principal o proveedores";
  private static final String DESC_NOMBRE = "Nombre del elemento";
  private static final String DESC_CUERPO = "Datos del elemento";

  private final Reenviador reenviador;

  public RabbitAdminController(
      RestClient.Builder restClientBuilder,
      @Value("${microservices.rabbit-admin.base-url}") String baseUrl) {
    this.reenviador = new Reenviador(restClientBuilder, baseUrl, "El administrador de RabbitMQ");
  }

  @Operation(summary = "Listar los brokers administrados", description = ADMIN)
  @ApiResponse(responseCode = "200", description = "Brokers",
      content = @Content(mediaType = JSON, schema = @Schema(type = "array")))
  @GetMapping("/brokers")
  public ResponseEntity<byte[]> brokers(HttpServletRequest request) {
    return reenviador.reenviar(request, null);
  }

  @Operation(summary = "Ver un broker", description = ADMIN + "Versión de RabbitMQ, clúster y nodo.",
      parameters = @Parameter(name = "broker", in = ParameterIn.PATH, required = true,
          description = DESC_BROKER, example = "principal"))
  @ApiResponse(responseCode = "200", description = "Datos del broker",
      content = @Content(mediaType = JSON, schema = @Schema(type = "object")))
  @GetMapping("/{broker}/overview")
  public ResponseEntity<byte[]> resumen(HttpServletRequest request) {
    return reenviador.reenviar(request, null);
  }

  @Operation(summary = "Listar las colas", description = ADMIN,
      parameters = @Parameter(name = "broker", in = ParameterIn.PATH, required = true,
          description = DESC_BROKER, example = "principal"))
  @ApiResponse(responseCode = "200", description = "Colas del broker",
      content = @Content(mediaType = JSON, schema = @Schema(type = "array")))
  @GetMapping("/{broker}/queues")
  public ResponseEntity<byte[]> listarColas(HttpServletRequest request) {
    return reenviador.reenviar(request, null);
  }

  @Operation(summary = "Crear una cola", description = ADMIN,
      parameters = @Parameter(name = "broker", in = ParameterIn.PATH, required = true,
          description = DESC_BROKER, example = "principal"),
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
          description = DESC_CUERPO,
          content = @Content(mediaType = JSON,
              examples = @ExampleObject(value = "{\"nombre\":\"q.cmd.demo\",\"durable\":true}"))))
  @ApiResponse(responseCode = "201", description = "Cola declarada")
  @ApiResponse(responseCode = "400", description = "Datos inválidos")
  @PostMapping("/{broker}/queues")
  public ResponseEntity<byte[]> crearCola(HttpServletRequest request, @RequestBody(required = false) byte[] cuerpo) {
    return reenviador.reenviar(request, cuerpo);
  }

  @Operation(summary = "Eliminar una cola", description = ADMIN,
      parameters = {
        @Parameter(name = "broker", in = ParameterIn.PATH, required = true, description = DESC_BROKER,
            example = "principal"),
        @Parameter(name = "nombre", in = ParameterIn.PATH, required = true, description = DESC_NOMBRE,
            example = "q.cmd.demo")
      })
  @ApiResponse(responseCode = "204", description = "Cola eliminada")
  @ApiResponse(responseCode = "404", description = "La cola no existe")
  @DeleteMapping("/{broker}/queues/{nombre}")
  public ResponseEntity<byte[]> eliminarCola(HttpServletRequest request) {
    return reenviador.reenviar(request, null);
  }

  @Operation(summary = "Listar los exchanges", description = ADMIN,
      parameters = {
        @Parameter(name = "broker", in = ParameterIn.PATH, required = true, description = DESC_BROKER,
            example = "principal"),
        @Parameter(name = "incluirSistema", in = ParameterIn.QUERY,
            description = "Incluir los exchanges propios de RabbitMQ (amq.*)", example = "false")
      })
  @ApiResponse(responseCode = "200", description = "Exchanges del broker",
      content = @Content(mediaType = JSON, schema = @Schema(type = "array")))
  @GetMapping("/{broker}/exchanges")
  public ResponseEntity<byte[]> listarExchanges(HttpServletRequest request) {
    return reenviador.reenviar(request, null);
  }

  @Operation(summary = "Crear un exchange", description = ADMIN + "Tipos: direct, topic, fanout o headers.",
      parameters = @Parameter(name = "broker", in = ParameterIn.PATH, required = true,
          description = DESC_BROKER, example = "principal"),
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
          description = DESC_CUERPO,
          content = @Content(mediaType = JSON,
              examples = @ExampleObject(value = "{\"nombre\":\"cmd.demo\",\"tipo\":\"direct\"}"))))
  @ApiResponse(responseCode = "201", description = "Exchange declarado")
  @ApiResponse(responseCode = "400", description = "Datos inválidos")
  @PostMapping("/{broker}/exchanges")
  public ResponseEntity<byte[]> crearExchange(
      HttpServletRequest request, @RequestBody(required = false) byte[] cuerpo) {
    return reenviador.reenviar(request, cuerpo);
  }

  @Operation(summary = "Eliminar un exchange", description = ADMIN,
      parameters = {
        @Parameter(name = "broker", in = ParameterIn.PATH, required = true, description = DESC_BROKER,
            example = "principal"),
        @Parameter(name = "nombre", in = ParameterIn.PATH, required = true, description = DESC_NOMBRE,
            example = "cmd.demo")
      })
  @ApiResponse(responseCode = "204", description = "Exchange eliminado")
  @ApiResponse(responseCode = "404", description = "El exchange no existe")
  @DeleteMapping("/{broker}/exchanges/{nombre}")
  public ResponseEntity<byte[]> eliminarExchange(HttpServletRequest request) {
    return reenviador.reenviar(request, null);
  }

  @Operation(summary = "Listar los bindings", description = ADMIN,
      parameters = @Parameter(name = "broker", in = ParameterIn.PATH, required = true,
          description = DESC_BROKER, example = "principal"))
  @ApiResponse(responseCode = "200", description = "Bindings del broker",
      content = @Content(mediaType = JSON, schema = @Schema(type = "array")))
  @GetMapping("/{broker}/bindings")
  public ResponseEntity<byte[]> listarBindings(HttpServletRequest request) {
    return reenviador.reenviar(request, null);
  }

  @Operation(summary = "Crear un binding", description = ADMIN + "Une un exchange con una cola mediante una routing key.",
      parameters = @Parameter(name = "broker", in = ParameterIn.PATH, required = true,
          description = DESC_BROKER, example = "principal"),
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
          description = DESC_CUERPO,
          content = @Content(mediaType = JSON,
              examples = @ExampleObject(
                  value = "{\"exchange\":\"cmd.demo\",\"cola\":\"q.cmd.demo\",\"routingKey\":\"demo.send\"}"))))
  @ApiResponse(responseCode = "201", description = "Binding creado")
  @ApiResponse(responseCode = "404", description = "El exchange o la cola no existen")
  @PostMapping("/{broker}/bindings")
  public ResponseEntity<byte[]> crearBinding(
      HttpServletRequest request, @RequestBody(required = false) byte[] cuerpo) {
    return reenviador.reenviar(request, cuerpo);
  }

  @Operation(summary = "Eliminar un binding", description = ADMIN,
      parameters = {
        @Parameter(name = "broker", in = ParameterIn.PATH, required = true, description = DESC_BROKER,
            example = "principal"),
        @Parameter(name = "exchange", in = ParameterIn.QUERY, required = true,
            description = "Exchange de origen", example = "cmd.demo"),
        @Parameter(name = "cola", in = ParameterIn.QUERY, required = true,
            description = "Cola de destino", example = "q.cmd.demo"),
        @Parameter(name = "routingKey", in = ParameterIn.QUERY,
            description = "Routing key del binding", example = "demo.send")
      })
  @ApiResponse(responseCode = "204", description = "Binding eliminado")
  @ApiResponse(responseCode = "404", description = "El binding no existe")
  @DeleteMapping("/{broker}/bindings")
  public ResponseEntity<byte[]> eliminarBinding(HttpServletRequest request) {
    return reenviador.reenviar(request, null);
  }
}
