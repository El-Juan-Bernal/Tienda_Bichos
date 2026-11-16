package hj.ms_kafka_admin.controller;

import hj.ms_kafka_admin.dto.ClusterResponse;
import hj.ms_kafka_admin.dto.TopicoRequest;
import hj.ms_kafka_admin.dto.TopicoResponse;
import hj.ms_kafka_admin.service.KafkaAdminService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kafka-admin")
public class KafkaAdminController {

    private final KafkaAdminService servicio;

    public KafkaAdminController(KafkaAdminService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/cluster")
    public ClusterResponse cluster() {
        return servicio.cluster();
    }

    @GetMapping("/topics")
    public List<TopicoResponse> listar() {
        return servicio.listar();
    }

    @GetMapping("/topics/{nombre}")
    public TopicoResponse obtener(@PathVariable String nombre) {
        return servicio.obtener(nombre);
    }

    @PostMapping("/topics")
    public ResponseEntity<Map<String, String>> crear(@Valid @RequestBody TopicoRequest request) {
        servicio.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of("mensaje", "Tópico creado", "nombre", request.nombre()));
    }

    /** Crea los 4 tópicos del caso si faltan (idempotente). */
    @PostMapping("/topics/base")
    public Map<String, List<String>> crearBase() {
        return Map.of("creados", servicio.crearTopicosBase());
    }

    @PutMapping("/topics/{nombre}/config")
    public ResponseEntity<Void> cambiarConfig(@PathVariable String nombre, @RequestBody Map<String, String> cambios) {
        servicio.cambiarConfiguracion(nombre, cambios);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/topics/{nombre}/particiones")
    public ResponseEntity<Void> aumentarParticiones(@PathVariable String nombre, @RequestBody Map<String, Integer> body) {
        servicio.aumentarParticiones(nombre, body.getOrDefault("total", 0));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/topics/{nombre}")
    public ResponseEntity<Void> eliminar(@PathVariable String nombre) {
        servicio.eliminar(nombre);
        return ResponseEntity.noContent().build();
    }
}

