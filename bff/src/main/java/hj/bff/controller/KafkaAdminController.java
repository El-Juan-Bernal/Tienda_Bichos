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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

// Administración de Kafka (tópicos). Solo Admin (lo define SecurityConfig). Reenvía a ms-kafka-admin.
@RestController
@RequestMapping("/api/kafka-admin")
@Tag(name = "Kafka admin (Admin)", description = "Clúster y tópicos de Kafka. Solo rol Admin.")
@ApiResponse(responseCode = "401", description = "Falta el token o no es válido")
@ApiResponse(responseCode = "403", description = "La cuenta no tiene el rol Admin")
public class KafkaAdminController {

  private static final String JSON = MediaType.APPLICATION_JSON_VALUE;
  private static final String ADMIN = "Requiere rol Admin. ";
  private static final String DESC_NOMBRE = "Nombre del tópico";
  private static final String DESC_NO_EXISTE = "El tópico no existe";
  private static final String EJ_TOPICO =
      "{\"nombre\":\"mi.topico\",\"particiones\":3,\"replicas\":3,\"configuraciones\":{\"cleanup.policy\":\"delete\"}}";

  private final Reenviador reenviador;

  public KafkaAdminController(
      RestClient.Builder restClientBuilder,
      @Value("${microservices.kafka-admin.base-url}") String baseUrl) {
    this.reenviador = new Reenviador(restClientBuilder, baseUrl, "El administrador de Kafka");
  }

  @Operation(summary = "Ver el clúster", description = ADMIN + "Id del clúster, controlador y brokers.")
  @ApiResponse(responseCode = "200", description = "Datos del clúster",
      content = @Content(mediaType = JSON, schema = @Schema(type = "object")))
  @GetMapping("/cluster")
  public ResponseEntity<byte[]> cluster(HttpServletRequest request) {
    return reenviador.reenviar(request, null);
  }

  @Operation(summary = "Listar los tópicos",
      description = ADMIN + "Con particiones, réplicas, líder e ISR de cada partición.")
  @ApiResponse(responseCode = "200", description = "Lista de tópicos",
      content = @Content(mediaType = JSON, schema = @Schema(type = "array")))
  @GetMapping("/topics")
  public ResponseEntity<byte[]> listar(HttpServletRequest request) {
    return reenviador.reenviar(request, null);
  }

  @Operation(summary = "Ver un tópico", description = ADMIN,
      parameters = @Parameter(name = "nombre", in = ParameterIn.PATH, required = true,
          description = DESC_NOMBRE, example = "orders.events"))
  @ApiResponse(responseCode = "200", description = "Datos del tópico",
      content = @Content(mediaType = JSON, schema = @Schema(type = "object")))
  @ApiResponse(responseCode = "404", description = DESC_NO_EXISTE)
  @GetMapping("/topics/{nombre}")
  public ResponseEntity<byte[]> obtener(HttpServletRequest request) {
    return reenviador.reenviar(request, null);
  }

  @Operation(summary = "Crear un tópico", description = ADMIN,
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
          description = "Nombre, particiones, réplicas y configuraciones del tópico",
          content = @Content(mediaType = JSON, examples = @ExampleObject(value = EJ_TOPICO))))
  @ApiResponse(responseCode = "201", description = "Tópico creado")
  @ApiResponse(responseCode = "400", description = "Datos o configuración inválidos")
  @ApiResponse(responseCode = "409", description = "El tópico ya existe")
  @PostMapping("/topics")
  public ResponseEntity<byte[]> crear(HttpServletRequest request, @RequestBody(required = false) byte[] cuerpo) {
    return reenviador.reenviar(request, cuerpo);
  }

  @Operation(summary = "Crear los tópicos base",
      description = ADMIN + "Crea los definidos en application.yaml de ms-kafka-admin que falten. "
          + "No modifica los que ya existen.")
  @ApiResponse(responseCode = "200", description = "Nombres de los tópicos creados",
      content = @Content(mediaType = JSON, schema = @Schema(type = "object")))
  @PostMapping("/topics/base")
  public ResponseEntity<byte[]> crearBase(HttpServletRequest request) {
    return reenviador.reenviar(request, null);
  }

  @Operation(summary = "Cambiar la configuración de un tópico", description = ADMIN,
      parameters = @Parameter(name = "nombre", in = ParameterIn.PATH, required = true,
          description = DESC_NOMBRE, example = "orders.events"),
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
          description = "Configuraciones de Kafka a cambiar",
          content = @Content(mediaType = JSON,
              examples = @ExampleObject(value = "{\"retention.ms\": \"604800000\"}"))))
  @ApiResponse(responseCode = "204", description = "Configuración actualizada")
  @ApiResponse(responseCode = "400", description = "Configuración inválida")
  @ApiResponse(responseCode = "404", description = DESC_NO_EXISTE)
  @PutMapping("/topics/{nombre}/config")
  public ResponseEntity<byte[]> cambiarConfig(
      HttpServletRequest request, @RequestBody(required = false) byte[] cuerpo) {
    return reenviador.reenviar(request, cuerpo);
  }

  @Operation(summary = "Aumentar las particiones de un tópico",
      description = ADMIN + "Solo se pueden aumentar, nunca reducir.",
      parameters = @Parameter(name = "nombre", in = ParameterIn.PATH, required = true,
          description = DESC_NOMBRE, example = "orders.events"),
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
          description = "Total de particiones que debe tener el tópico",
          content = @Content(mediaType = JSON, examples = @ExampleObject(value = "{\"total\": 6}"))))
  @ApiResponse(responseCode = "204", description = "Particiones aumentadas")
  @ApiResponse(responseCode = "400", description = "Número de particiones inválido")
  @ApiResponse(responseCode = "404", description = DESC_NO_EXISTE)
  @PutMapping("/topics/{nombre}/particiones")
  public ResponseEntity<byte[]> aumentarParticiones(
      HttpServletRequest request, @RequestBody(required = false) byte[] cuerpo) {
    return reenviador.reenviar(request, cuerpo);
  }

  @Operation(summary = "Eliminar un tópico", description = ADMIN,
      parameters = @Parameter(name = "nombre", in = ParameterIn.PATH, required = true,
          description = DESC_NOMBRE, example = "mi.topico"))
  @ApiResponse(responseCode = "204", description = "Tópico eliminado")
  @ApiResponse(responseCode = "404", description = DESC_NO_EXISTE)
  @DeleteMapping("/topics/{nombre}")
  public ResponseEntity<byte[]> eliminar(HttpServletRequest request) {
    return reenviador.reenviar(request, null);
  }
}
