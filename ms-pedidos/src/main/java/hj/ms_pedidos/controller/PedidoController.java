package hj.ms_pedidos.controller;

import hj.ms_pedidos.dto.ActualizarEstadoRequest;
import hj.ms_pedidos.dto.CrearPedidoRequest;
import hj.ms_pedidos.entity.EstadoPedido;
import hj.ms_pedidos.entity.Pedido;
import hj.ms_pedidos.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

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

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Object> actualizarEstado(
            @PathVariable Long id,
            @RequestBody ActualizarEstadoRequest request) {
        try {
            Pedido actualizado = pedidoService.actualizarEstado(id, request.nuevoEstado());

            // Si el pedido acaba de ser ACEPTADO, iniciamos la simulación automática cada 5 segundos
            if (request.nuevoEstado() == EstadoPedido.ACEPTADO) {
                iniciarAvanceAutomatico(id);
            }

            return ResponseEntity.ok(actualizado);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private void iniciarAvanceAutomatico(Long pedidoId) {
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PedidoController.class);
        CompletableFuture.runAsync(() -> {
            EstadoPedido[] siguientesEstados = {
                EstadoPedido.EN_PREPARACION,
                EstadoPedido.DESPACHADO,
                EstadoPedido.ENTREGADO
            };

            boolean continuar = true;
            for (EstadoPedido siguiente : siguientesEstados) {
                if (!continuar) {
                    break;
                }
                try {
                    TimeUnit.SECONDS.sleep(5);
                    pedidoService.actualizarEstado(pedidoId, siguiente);
                } catch (InterruptedException _) {
                    Thread.currentThread().interrupt();
                    continuar = false;
                } catch (Exception e) {
                    log.error("Deteniendo avance automático para pedido #{}: {}", pedidoId, e.getMessage());
                    continuar = false;
                }
            }
        });
    }
}

