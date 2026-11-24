package hj.ms_reportes.controller;

import hj.ms_reportes.dto.KpisResponse;
import hj.ms_reportes.dto.TopProducto;
import hj.ms_reportes.service.ReportesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Solo lectura: los datos se calculan por streaming desde orders.events. */
@RestController
@RequestMapping("/api/report")
@Tag(name = "Reportes", description = "KPIs y productos más vendidos, calculados por streaming (solo lectura)")
public class ReportesController {

    private static final String DESC_RANGO =
        "Rango de tiempo: lastNh (horas) o lastNd (días), por ejemplo last24h o last7d";

    private final ReportesService servicio;

    public ReportesController(ReportesService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "KPIs del período",
        description = "Pedidos vendidos, ventas totales, ticket promedio, lead time promedio, "
            + "pedidos entregados y activos, pedidos por estado y ventas por hora (hora de Chile).")
    @GetMapping("/kpis")
    public KpisResponse kpis(
            @Parameter(description = DESC_RANGO, example = "last24h")
            @RequestParam(defaultValue = "last24h") String range) {
        return servicio.kpis(range);
    }

    @Operation(summary = "Productos más vendidos",
        description = "Unidades e ingresos brutos por producto (antes de descuentos). "
            + "Las cancelaciones se descuentan. Ordenado de mayor a menor.")
    @GetMapping("/top-products")
    public List<TopProducto> topProductos(
            @Parameter(description = DESC_RANGO, example = "last7d")
            @RequestParam(defaultValue = "last7d") String range,
            @Parameter(description = "Cantidad máxima de productos a devolver")
            @RequestParam(defaultValue = "10") int limit) {
        return servicio.topProductos(range, limit);
    }
}

