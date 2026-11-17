package hj.ms_auditoria.controller;

import hj.ms_auditoria.dto.EventoAuditoriaResponse;
import hj.ms_auditoria.dto.PaginaResponse;
import hj.ms_auditoria.service.AuditoriaService;
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
public class AuditoriaController {

    private final AuditoriaService servicio;

    public AuditoriaController(AuditoriaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/orders/{pedidoId}/timeline")
    public List<EventoAuditoriaResponse> timeline(@PathVariable Long pedidoId) {
        return servicio.timelineDePedido(pedidoId);
    }

    /** Filtros opcionales: usuario, tipo (OrderCreated...), desde y hasta (ISO, ej. 2026-11-16T00:00:00Z). */
    @GetMapping("/events")
    public PaginaResponse<EventoAuditoriaResponse> eventos(
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Instant desde,
            @RequestParam(required = false) Instant hasta,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        return servicio.buscar(usuario, tipo, desde, hasta, pagina, tamano);
    }
}

