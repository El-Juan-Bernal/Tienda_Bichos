package hj.ms_pedidos.controller;

import hj.ms_pedidos.dto.ActualizarEstadoRequest;
import hj.ms_pedidos.dto.CrearPedidoRequest;
import hj.ms_pedidos.entity.EstadoPedido;
import hj.ms_pedidos.entity.Pedido;
import hj.ms_pedidos.entity.PedidoHistorial;
import hj.ms_pedidos.service.AvanceAutomaticoPedidos;
import hj.ms_pedidos.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Creación, consulta, cambio de estado, cancelación e historial de pedidos")
public class PedidoController {

    private static final String DESC_ID = "Id del pedido";

    private final PedidoService pedidoService;
    private final AvanceAutomaticoPedidos avanceAutomatico;

    @Operation(summary = "Crear un pedido",
        description = "Crea el pedido en estado CREADO y publica el evento OrderCreated en Kafka.")
    @ApiResponse(responseCode = "201", description = "Pedido creado")
    @PostMapping
    public ResponseEntity<Pedido> crear(@RequestBody CrearPedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crearPedido(request));
    }

    @Operation(summary = "Listar pedidos",
        description = "Con el parámetro email devuelve solo los pedidos de ese usuario; sin él, todos.")
    @GetMapping
    public ResponseEntity<List<Pedido>> listar(
            @Parameter(description = "Correo del dueño de los pedidos (opcional)")
            @RequestParam(required = false) String email) {
        if (email != null && !email.isBlank()) {
            return ResponseEntity.ok(pedidoService.listarPorUsuario(email));
        }
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    @Operation(summary = "Historial de estados de un pedido")
    @GetMapping("/{id}/historial")
    public ResponseEntity<List<PedidoHistorial>> historial(@Parameter(description = DESC_ID) @PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.listarHistorial(id));
    }

    @Operation(summary = "Cambiar el estado de un pedido",
        description = "Estados: CREADO, ACEPTADO, EN_PREPARACION, DESPACHADO, ENTREGADO y CANCELADO. "
            + "Al pasar a ACEPTADO se descuenta el stock y comienza el avance automático de estados.")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "400", description = "Transición no permitida o pedido inexistente")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Object> actualizarEstado(
            @Parameter(description = DESC_ID) @PathVariable Long id,
            @RequestBody ActualizarEstadoRequest request) {
        try {
            Pedido actualizado = pedidoService.actualizarEstado(id, request.nuevoEstado());

            // Al ACEPTAR el pedido se inicia la simulación automática de avance
            if (request.nuevoEstado() == EstadoPedido.ACEPTADO) {
                avanceAutomatico.iniciar(id);
            }
            return ResponseEntity.ok(actualizado);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Cancelar un pedido",
        description = "Solo se puede cancelar desde ACEPTADO y EN_PREPARACION. Genera un reembolso simulado. "
            + "El BFF envía el correo del token en 'usuario' para validar que el pedido es del solicitante.")
    @ApiResponse(responseCode = "200", description = "Pedido cancelado")
    @ApiResponse(responseCode = "400", description = "El pedido no se puede cancelar en su estado actual")
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Object> cancelar(
            @Parameter(description = DESC_ID) @PathVariable Long id,
            @Parameter(description = "Correo del dueño del pedido (opcional)")
            @RequestParam(required = false) String usuario) {
        try {
            return ResponseEntity.ok(pedidoService.cancelarPedido(id, usuario));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

