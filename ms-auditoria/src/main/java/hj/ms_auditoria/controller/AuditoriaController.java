package hj.ms_auditoria.controller;

import hj.ms_auditoria.dto.EventoAuditoriaResponse;
import hj.ms_auditoria.dto.PaginaResponse;
import hj.ms_auditoria.service.AuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Solo lectura: no hay endpoints que modifiquen la auditoría. */
@RestController
@RequestMapping("/api/audit")
@Tag(name = "Auditoría", description = "Trazabilidad de pedidos: quién, qué, cuándo y desde dónde (solo lectura)")
public class AuditoriaController {

    private final AuditoriaService servicio;

    public AuditoriaController(AuditoriaService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Línea de tiempo de un pedido",
        description = "Todos los eventos del pedido, desde que se crea hasta su estado actual.")
    @GetMapping("/orders/{pedidoId}/timeline")
    public List<EventoAuditoriaResponse> timeline(
            @Parameter(description = "Id del pedido") @PathVariable Long pedidoId) {
        return servicio.timelineDePedido(pedidoId);
    }

    @Operation(summary = "Buscar eventos",
        description = "Lista paginada de eventos, del más reciente al más antiguo. Todos los filtros son opcionales.")
    @GetMapping("/events")
    public PaginaResponse<EventoAuditoriaResponse> eventos(
            @Parameter(description = "Correo del usuario que originó el evento")
            @RequestParam(required = false) String usuario,
            @Parameter(description = "Tipo de evento: OrderCreated, OrderAccepted, OrderPreparing, "
                + "OrderDispatched, OrderDelivered u OrderCancelled")
            @RequestParam(required = false) String tipo,
            @Parameter(description = "Desde (instante ISO en UTC)", example = "2026-11-16T00:00:00Z")
            @RequestParam(required = false) Instant desde,
            @Parameter(description = "Hasta (instante ISO en UTC)", example = "2026-11-18T23:59:59Z")
            @RequestParam(required = false) Instant hasta,
            @Parameter(description = "Número de página, desde 0") @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Eventos por página (máximo 100)") @RequestParam(defaultValue = "20") int tamano) {
        return servicio.buscar(usuario, tipo, desde, hasta, pagina, tamano);
    }
}

