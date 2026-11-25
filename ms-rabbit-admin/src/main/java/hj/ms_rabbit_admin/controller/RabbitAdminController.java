package hj.ms_rabbit_admin.controller;

import hj.ms_rabbit_admin.dto.BindingRequest;
import hj.ms_rabbit_admin.dto.BindingResponse;
import hj.ms_rabbit_admin.dto.BrokerResponse;
import hj.ms_rabbit_admin.dto.ColaRequest;
import hj.ms_rabbit_admin.dto.ColaResponse;
import hj.ms_rabbit_admin.dto.ExchangeRequest;
import hj.ms_rabbit_admin.dto.ExchangeResponse;
import hj.ms_rabbit_admin.service.RabbitAdminService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rabbit-admin")
@Tag(name = "RabbitMQ admin", description = "Colas, exchanges y bindings de los dos brokers de RabbitMQ")
public class RabbitAdminController {

    private static final String DESC_BROKER = "Broker: principal (pedidos, correos, bodega y stock) o proveedores";
    private static final String DESC_SISTEMA = "Incluir los elementos propios de RabbitMQ (amq.* y el exchange por defecto)";
    private static final String MENSAJE = "mensaje";
    private static final String NOMBRE = "nombre";

    private final RabbitAdminService servicio;

    public RabbitAdminController(RabbitAdminService servicio) {
        this.servicio = servicio;
    }

    // ------------------------------------------------------------------ brokers

    @Operation(summary = "Listar los brokers administrados")
    @GetMapping("/brokers")
    public List<BrokerResponse> brokers() {
        return servicio.listarBrokers();
    }

    @Operation(summary = "Ver un broker", description = "Versión de RabbitMQ, nombre del clúster y nodo.")
    @GetMapping("/{broker}/overview")
    public Map<String, Object> resumen(@Parameter(description = DESC_BROKER) @PathVariable String broker) {
        return servicio.resumen(broker);
    }

    // ------------------------------------------------------------------ colas

    @Operation(summary = "Listar las colas",
        description = "Con mensajes pendientes, consumidores y argumentos (por ejemplo la DLQ asociada).")
    @GetMapping("/{broker}/queues")
    public List<ColaResponse> listarColas(@Parameter(description = DESC_BROKER) @PathVariable String broker) {
        return servicio.listarColas(broker);
    }

    @Operation(summary = "Crear una cola",
        description = "Si ya existe con la misma configuración no hace nada; si existe con otra, responde 400.")
    @ApiResponse(responseCode = "201", description = "Cola declarada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o la cola existe con otra configuración")
    @PostMapping("/{broker}/queues")
    public ResponseEntity<Map<String, String>> crearCola(
            @Parameter(description = DESC_BROKER) @PathVariable String broker,
            @Valid @RequestBody ColaRequest request) {
        servicio.crearCola(broker, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(MENSAJE, "Cola declarada", NOMBRE, request.nombre()));
    }

    @Operation(summary = "Eliminar una cola")
    @ApiResponse(responseCode = "204", description = "Cola eliminada")
    @ApiResponse(responseCode = "404", description = "La cola no existe")
    @DeleteMapping("/{broker}/queues/{nombre}")
    public ResponseEntity<Void> eliminarCola(
            @Parameter(description = DESC_BROKER) @PathVariable String broker,
            @Parameter(description = "Nombre de la cola") @PathVariable String nombre) {
        servicio.eliminarCola(broker, nombre);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------ exchanges

    @Operation(summary = "Listar los exchanges")
    @GetMapping("/{broker}/exchanges")
    public List<ExchangeResponse> listarExchanges(
            @Parameter(description = DESC_BROKER) @PathVariable String broker,
            @Parameter(description = DESC_SISTEMA) @RequestParam(defaultValue = "false") boolean incluirSistema) {
        return servicio.listarExchanges(broker, incluirSistema);
    }

    @Operation(summary = "Crear un exchange", description = "Tipos: direct, topic, fanout o headers.")
    @ApiResponse(responseCode = "201", description = "Exchange declarado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o el exchange existe con otra configuración")
    @PostMapping("/{broker}/exchanges")
    public ResponseEntity<Map<String, String>> crearExchange(
            @Parameter(description = DESC_BROKER) @PathVariable String broker,
            @Valid @RequestBody ExchangeRequest request) {
        servicio.crearExchange(broker, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(MENSAJE, "Exchange declarado", NOMBRE, request.nombre()));
    }

    @Operation(summary = "Eliminar un exchange")
    @ApiResponse(responseCode = "204", description = "Exchange eliminado")
    @ApiResponse(responseCode = "404", description = "El exchange no existe")
    @DeleteMapping("/{broker}/exchanges/{nombre}")
    public ResponseEntity<Void> eliminarExchange(
            @Parameter(description = DESC_BROKER) @PathVariable String broker,
            @Parameter(description = "Nombre del exchange") @PathVariable String nombre) {
        servicio.eliminarExchange(broker, nombre);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------ bindings

    @Operation(summary = "Listar los bindings", description = "Qué exchange envía a qué cola y con qué routing key.")
    @GetMapping("/{broker}/bindings")
    public List<BindingResponse> listarBindings(
            @Parameter(description = DESC_BROKER) @PathVariable String broker,
            @Parameter(description = DESC_SISTEMA) @RequestParam(defaultValue = "false") boolean incluirSistema) {
        return servicio.listarBindings(broker, incluirSistema);
    }

    @Operation(summary = "Crear un binding", description = "Une un exchange con una cola mediante una routing key.")
    @ApiResponse(responseCode = "201", description = "Binding creado")
    @ApiResponse(responseCode = "404", description = "El exchange o la cola no existen")
    @PostMapping("/{broker}/bindings")
    public ResponseEntity<Map<String, String>> crearBinding(
            @Parameter(description = DESC_BROKER) @PathVariable String broker,
            @Valid @RequestBody BindingRequest request) {
        servicio.crearBinding(broker, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(MENSAJE, "Binding creado"));
    }

    @Operation(summary = "Eliminar un binding")
    @ApiResponse(responseCode = "204", description = "Binding eliminado")
    @ApiResponse(responseCode = "404", description = "El binding no existe")
    @DeleteMapping("/{broker}/bindings")
    public ResponseEntity<Void> eliminarBinding(
            @Parameter(description = DESC_BROKER) @PathVariable String broker,
            @Parameter(description = "Exchange de origen") @RequestParam String exchange,
            @Parameter(description = "Cola de destino") @RequestParam String cola,
            @Parameter(description = "Routing key del binding (vacía si no tiene)") @RequestParam(defaultValue = "") String routingKey) {
        servicio.eliminarBinding(broker, exchange, cola, routingKey);
        return ResponseEntity.noContent().build();
    }
}
