package hj.bff.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final RestClient msPedidosClient;

    public PedidoController(
            RestClient.Builder restClientBuilder,
            @Value("${microservices.pedidos.base-url}") String msPedidosBaseUrl) {
        this.msPedidosClient = restClientBuilder
                .baseUrl(msPedidosBaseUrl)
                .build();
    }

    @PostMapping
    public ResponseEntity<Object> procesarPedido(@RequestBody Object pedidoRequest) {
        try {
            // Se captura el cuerpo de la respuesta que contiene el JSON del pedido creado
            Object respuestaMsPedidos = msPedidosClient.post()
                    .uri("/api/pedidos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(pedidoRequest)
                    .retrieve()
                    .body(Object.class);

            // Se retorna el JSON real al frontend
            return ResponseEntity.ok(respuestaMsPedidos);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error al procesar el pedido: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<String> actualizarEstado(@PathVariable Long id, @RequestBody Object estadoRequest) {
        try {
            msPedidosClient.patch()
                    .uri("/api/pedidos/{id}/estado", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(estadoRequest)
                    .retrieve()
                    .toBodilessEntity();

            return ResponseEntity.ok("¡Estado del pedido actualizado con éxito!");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error al actualizar el estado: " + e.getMessage());
        }
    }
}

