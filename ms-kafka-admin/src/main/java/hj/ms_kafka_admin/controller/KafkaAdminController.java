package hj.ms_kafka_admin.controller;

import hj.ms_kafka_admin.dto.ClusterResponse;
import hj.ms_kafka_admin.dto.TopicoRequest;
import hj.ms_kafka_admin.dto.TopicoResponse;
import hj.ms_kafka_admin.service.KafkaAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kafka-admin")
@Tag(name = "Kafka admin", description = "Administración del clúster Kafka: tópicos, configuraciones y particiones")
public class KafkaAdminController {

    private static final String DESC_NOMBRE = "Nombre del tópico";
    private static final String DESC_NO_EXISTE = "El tópico no existe";

    private final KafkaAdminService servicio;

    public KafkaAdminController(KafkaAdminService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Ver el clúster",
        description = "Id del clúster, broker controlador y lista de brokers.")
    @GetMapping("/cluster")
    public ClusterResponse cluster() {
        return servicio.cluster();
    }

    @Operation(summary = "Listar los tópicos",
        description = "Con sus particiones, réplicas, líder e ISR de cada partición.")
    @GetMapping("/topics")
    public List<TopicoResponse> listar() {
        return servicio.listar();
    }

    @Operation(summary = "Ver un tópico")
    @ApiResponse(responseCode = "404", description = DESC_NO_EXISTE)
    @GetMapping("/topics/{nombre}")
    public TopicoResponse obtener(@Parameter(description = DESC_NOMBRE) @PathVariable String nombre) {
        return servicio.obtener(nombre);
    }

    @Operation(summary = "Crear un tópico")
    @ApiResponse(responseCode = "201", description = "Tópico creado")
    @ApiResponse(responseCode = "400", description = "Datos o configuración inválidos")
    @ApiResponse(responseCode = "409", description = "El tópico ya existe")
    @PostMapping("/topics")
    public ResponseEntity<Map<String, String>> crear(@Valid @RequestBody TopicoRequest request) {
        servicio.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of("mensaje", "Tópico creado", "nombre", request.nombre()));
    }

    @Operation(summary = "Crear los tópicos base",
        description = "Crea los tópicos definidos en application.yaml que falten. "
            + "Es idempotente: no modifica los que ya existen. Devuelve los nombres creados.")
    @PostMapping("/topics/base")
    public Map<String, List<String>> crearBase() {
        return Map.of("creados", servicio.crearTopicosBase());
    }

    @Operation(summary = "Cambiar la configuración de un tópico",
        description = "Cuerpo: mapa de configuraciones de Kafka, por ejemplo {\"retention.ms\": \"604800000\"}.")
    @ApiResponse(responseCode = "204", description = "Configuración actualizada")
    @ApiResponse(responseCode = "400", description = "Configuración inválida")
    @ApiResponse(responseCode = "404", description = DESC_NO_EXISTE)
    @PutMapping("/topics/{nombre}/config")
    public ResponseEntity<Void> cambiarConfig(
            @Parameter(description = DESC_NOMBRE) @PathVariable String nombre,
            @RequestBody Map<String, String> cambios) {
        servicio.cambiarConfiguracion(nombre, cambios);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Aumentar las particiones de un tópico",
        description = "Cuerpo: {\"total\": 6}. Solo se pueden aumentar, nunca reducir.")
    @ApiResponse(responseCode = "204", description = "Particiones aumentadas")
    @ApiResponse(responseCode = "400", description = "Número de particiones inválido")
    @ApiResponse(responseCode = "404", description = DESC_NO_EXISTE)
    @PutMapping("/topics/{nombre}/particiones")
    public ResponseEntity<Void> aumentarParticiones(
            @Parameter(description = DESC_NOMBRE) @PathVariable String nombre,
            @RequestBody Map<String, Integer> body) {
        servicio.aumentarParticiones(nombre, body.getOrDefault("total", 0));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Eliminar un tópico")
    @ApiResponse(responseCode = "204", description = "Tópico eliminado")
    @ApiResponse(responseCode = "404", description = DESC_NO_EXISTE)
    @DeleteMapping("/topics/{nombre}")
    public ResponseEntity<Void> eliminar(@Parameter(description = DESC_NOMBRE) @PathVariable String nombre) {
        servicio.eliminar(nombre);
        return ResponseEntity.noContent().build();
    }
}

