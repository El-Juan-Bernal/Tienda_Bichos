package hj.ms_pedidos.controller;

import hj.ms_pedidos.dto.ActualizarEstadoRequest;
import hj.ms_pedidos.dto.CrearPedidoRequest;
import hj.ms_pedidos.entity.EstadoPedido;
import hj.ms_pedidos.entity.Pedido;
import hj.ms_pedidos.entity.PedidoHistorial;
import hj.ms_pedidos.service.AvanceAutomaticoPedidos;
import hj.ms_pedidos.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final AvanceAutomaticoPedidos avanceAutomatico;

    @PostMapping
    public ResponseEntity<Pedido> crear(@RequestBody CrearPedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crearPedido(request));
    }

    @GetMapping
    public ResponseEntity<List<Pedido>> listar(@RequestParam(required = false) String email) {
        if (email != null && !email.isBlank()) {
            return ResponseEntity.ok(pedidoService.listarPorUsuario(email));
        }
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<PedidoHistorial>> historial(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.listarHistorial(id));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Object> actualizarEstado(
            @PathVariable Long id,
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

    /** 'usuario' es opcional aquí; el BFF lo enviará con el correo del token para validar al dueño. */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Object> cancelar(
            @PathVariable Long id,
            @RequestParam(required = false) String usuario) {
        try {
            return ResponseEntity.ok(pedidoService.cancelarPedido(id, usuario));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

