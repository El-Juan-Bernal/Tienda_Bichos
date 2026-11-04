package hj.ms_catalogo.controller;

import hj.ms_catalogo.dto.ProductoRequest;
import hj.ms_catalogo.dto.ProductoResponse;
import hj.ms_catalogo.entity.Producto;
import hj.ms_catalogo.exception.DestacadoNoPermitidoException;
import hj.ms_catalogo.service.ProductoService;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<ProductoResponse> listar() {
        return productoService.obtenerTodos();
    }

    // NUEVO: productos activos con la estrella encendida (máximo 8)
    @GetMapping("/destacados")
    public List<ProductoResponse> destacados() {
        return productoService.obtenerDestacados();
    }

    @GetMapping("/{id}")
    public ProductoResponse buscarPorId(@PathVariable Long id) {
        return productoService.buscarPorId(id);
    }

    @PutMapping("/{id}/descontar-stock")
    public ResponseEntity<Void> descontarStock(@PathVariable Long id, @RequestParam Integer cantidad) {
        productoService.descontarStock(id, cantidad);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizarProducto(@PathVariable Long id, @RequestBody ProductoRequest request) {
        ProductoResponse productoActualizado = productoService.actualizarProducto(id, request);
        return ResponseEntity.ok(productoActualizado);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        Boolean activo = body.getOrDefault("activo", false);
        productoService.cambiarEstado(id, activo);
        return ResponseEntity.ok().build();
    }

    // NUEVO: enciende o apaga la estrella. 409 si ya hay 8 o si el producto está deshabilitado.
    @PutMapping("/{id}/destacado")
    public ResponseEntity<Object> cambiarDestacado(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean destacado = Boolean.TRUE.equals(body.get("destacado"));
        try {
            return ResponseEntity.ok(productoService.cambiarDestacado(id, destacado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (DestacadoNoPermitidoException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/productos/masivo
    @PostMapping("/masivo")
    public ResponseEntity<List<ProductoResponse>> crearProductosMasivamente(@RequestBody List<Producto> productos) {
        List<ProductoResponse> nuevosProductos = productoService.guardarMasivo(productos);
        return new ResponseEntity<>(nuevosProductos, HttpStatus.CREATED);
    }
}

