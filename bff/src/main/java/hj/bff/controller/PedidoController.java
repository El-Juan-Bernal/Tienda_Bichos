package hj.bff.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos",
    description = "Pedidos del usuario autenticado. El dueño sale siempre del token, nunca del cuerpo de la petición.")
public class PedidoController {

    private static final String[] CLAIMS_CORREO = {"preferred_username", "email", "upn", "unique_name"};
    private static final String CLAVE_ERROR = "error";
    private static final String MSG_NO_ENCONTRADO = "Pedido no encontrado.";
    private static final String DESC_ID = "Id del pedido";

    private final RestClient msPedidosClient;

    public PedidoController(
            RestClient.Builder restClientBuilder,
            @Value("${microservices.pedidos.base-url}") String msPedidosBaseUrl) {
        this.msPedidosClient = restClientBuilder
                .baseUrl(msPedidosBaseUrl)
                .build();
    }

        // Crear pedido: el dueño siempre es el usuario del token, no el que envíe el front
    @Operation(summary = "Crear un pedido",
        description = "Requiere un usuario autenticado. El campo 'usuario' se reemplaza por el correo del token.")
    @PostMapping
    public ResponseEntity<Object> procesarPedido(
            @RequestBody Map<String, Object> pedidoRequest,
            @AuthenticationPrincipal Jwt jwt) {
        String correo = correoDe(jwt);
        if (correo == null) {
            return sinCorreo();
        }
        try {
            pedidoRequest.put("usuario", correo);
            Object respuestaMsPedidos = msPedidosClient.post()
                    .uri("/api/pedidos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(pedidoRequest)
                    .retrieve()
                    .body(Object.class);
            return ResponseEntity.ok(respuestaMsPedidos);
        } catch (RestClientResponseException e) {
            return reenviarError(e);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error al procesar el pedido: " + e.getMessage());
        }
    }

    // Mis pedidos: siempre filtrados por el correo del token
    @Operation(summary = "Mis pedidos",
        description = "Devuelve solo los pedidos del usuario del token.")
    @GetMapping
    public ResponseEntity<Object> misPedidos(@AuthenticationPrincipal Jwt jwt) {
        String correo = correoDe(jwt);
        if (correo == null) {
            return sinCorreo();
        }
        try {
            return ResponseEntity.ok(pedidosDe(correo));
        } catch (RestClientResponseException e) {
            return reenviarError(e);
        } catch (Exception e) {
            return ResponseEntity.status(502)
                    .body(Map.of(CLAVE_ERROR, "No se pudieron consultar los pedidos: " + e.getMessage()));
        }
    }

    @Operation(summary = "Historial de estados de un pedido",
        description = "Solo si el pedido pertenece al usuario del token; si no, responde 404.")
    @ApiResponse(responseCode = "404", description = "El pedido no existe o no es del usuario")
    @GetMapping("/{id}/historial")
    public ResponseEntity<Object> historial(
            @Parameter(description = DESC_ID) @PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String correo = correoDe(jwt);
        if (correo == null) {
            return sinCorreo();
        }
        try {
            if (!esDueno(id, correo)) {
                return ResponseEntity.status(404).body(Map.of(CLAVE_ERROR, MSG_NO_ENCONTRADO));
            }
            Object historial = msPedidosClient.get()
                    .uri("/api/pedidos/{id}/historial", id)
                    .retrieve()
                    .body(Object.class);
            return ResponseEntity.ok(historial);
        } catch (RestClientResponseException e) {
            return reenviarError(e);
        } catch (Exception e) {
            return ResponseEntity.status(502)
                    .body(Map.of(CLAVE_ERROR, "No se pudo consultar el historial: " + e.getMessage()));
        }
    }

    // El cliente solo puede ACEPTAR (pagar) o CANCELAR; el resto de los estados avanza solo
    @Operation(summary = "Aceptar (pagar) o cancelar un pedido",
        description = "Cuerpo: {\"nuevoEstado\": \"ACEPTADO\"} o {\"nuevoEstado\": \"CANCELADO\"}. "
            + "Los demás estados avanzan solos.")
    @ApiResponse(responseCode = "403", description = "El cliente solo puede aceptar o cancelar")
    @ApiResponse(responseCode = "404", description = "El pedido no existe o no es del usuario")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Object> actualizarEstado(
            @Parameter(description = DESC_ID) @PathVariable Long id,
            @RequestBody Map<String, Object> estadoRequest,
            @AuthenticationPrincipal Jwt jwt) {
        String correo = correoDe(jwt);
        if (correo == null) {
            return sinCorreo();
        }

        Object valor = estadoRequest.get("nuevoEstado");
        String nuevoEstado = valor != null ? valor.toString() : "";

        if ("CANCELADO".equals(nuevoEstado)) {
            return cancelarPedido(id, correo);
        }
        if (!"ACEPTADO".equals(nuevoEstado)) {
            return ResponseEntity.status(403).body(Map.of(CLAVE_ERROR,
                    "El cliente solo puede aceptar o cancelar un pedido; los demás estados avanzan automáticamente."));
        }

        try {
            if (!esDueno(id, correo)) {
                return ResponseEntity.status(404).body(Map.of(CLAVE_ERROR, MSG_NO_ENCONTRADO));
            }
            msPedidosClient.patch()
                    .uri("/api/pedidos/{id}/estado", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(estadoRequest)
                    .retrieve()
                    .toBodilessEntity();
            return ResponseEntity.ok("¡Estado del pedido actualizado con éxito!");
        } catch (RestClientResponseException e) {
            return reenviarError(e);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error al actualizar el estado: " + e.getMessage());
        }
    }

    @Operation(summary = "Cancelar un pedido",
        description = "Solo desde ACEPTADO o EN_PREPARACION, y solo si el pedido es del usuario del token.")
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Object> cancelar(
            @Parameter(description = DESC_ID) @PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String correo = correoDe(jwt);
        if (correo == null) {
            return sinCorreo();
        }
        return cancelarPedido(id, correo);
    }

    private ResponseEntity<Object> cancelarPedido(Long id, String correo) {
        try {
            Object pedido = msPedidosClient.patch()
                    .uri(b -> b.path("/api/pedidos/{id}/cancelar")
                            .queryParam("usuario", "{usuario}")
                            .build(id, correo))
                    .retrieve()
                    .body(Object.class);
            return ResponseEntity.ok(pedido);
        } catch (RestClientResponseException e) {
            return reenviarError(e);
        } catch (Exception e) {
            return ResponseEntity.status(502)
                    .body(Map.of(CLAVE_ERROR, "No se pudo cancelar el pedido: " + e.getMessage()));
        }
    }

    private List<Map<String, Object>> pedidosDe(String correo) {
        List<Map<String, Object>> pedidos = msPedidosClient.get()
                .uri(b -> b.path("/api/pedidos").queryParam("email", "{email}").build(correo))
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        return pedidos != null ? pedidos : List.of();
    }

    private boolean esDueno(Long id, String correo) {
        return pedidosDe(correo).stream()
                .anyMatch(p -> p.get("id") instanceof Number n && n.longValue() == id);
    }

    private String correoDe(Jwt jwt) {
        if (jwt == null) {
            return null;
        }
        for (String claim : CLAIMS_CORREO) {
            String valor = jwt.getClaimAsString(claim);
            if (valor != null && !valor.isBlank()) {
                return valor;
            }
        }
        return null;
    }

    private ResponseEntity<Object> sinCorreo() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of(CLAVE_ERROR, "El token no incluye el correo del usuario."));
    }

    // Reenvía al front el estado y el mensaje que devolvió ms-pedidos
    private ResponseEntity<Object> reenviarError(RestClientResponseException e) {
        String cuerpo = e.getResponseBodyAsString();
        Object respuesta = cuerpo.isBlank()
                ? Map.of(CLAVE_ERROR, "Solicitud rechazada por el servicio de pedidos.")
                : cuerpo;
        return ResponseEntity.status(e.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(respuesta);
    }
}
