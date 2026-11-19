package hj.ms_reportes.controller;

import hj.ms_reportes.dto.KpisResponse;
import hj.ms_reportes.dto.TopProducto;
import hj.ms_reportes.service.ReportesService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Solo lectura: los datos se calculan por streaming desde orders.events. */
@RestController
@RequestMapping("/api/report")
public class ReportesController {

    private final ReportesService servicio;

    public ReportesController(ReportesService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/kpis")
    public KpisResponse kpis(@RequestParam(defaultValue = "last24h") String range) {
        return servicio.kpis(range);
    }

    @GetMapping("/top-products")
    public List<TopProducto> topProductos(@RequestParam(defaultValue = "last7d") String range,
                                          @RequestParam(defaultValue = "10") int limit) {
        return servicio.topProductos(range, limit);
    }
}

