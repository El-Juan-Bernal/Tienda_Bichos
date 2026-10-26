package hj.bff.controller;

import hj.bff.dto.ProductoResponse;
import hj.bff.service.ProductoService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

  @GetMapping("/{id}")
  public ProductoResponse buscarPorId(@PathVariable Long id) {
    return productoService.obtenerPorId(id);
  }

  @PostMapping
  public ProductoResponse crear(@RequestBody ProductoResponse request) {
    return productoService.crear(request);
  }

  @PostMapping("/masivo")
  public List<ProductoResponse> crearMasivo(@RequestBody List<ProductoResponse> requests) {
    return productoService.crearMasivo(requests);
  }

  @PutMapping("/{id}")
  public ProductoResponse actualizar(@PathVariable Long id, @RequestBody ProductoResponse request) {
    return productoService.actualizar(id, request);
  }

  @PutMapping("/{id}/estado")
  public void cambiarEstado(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
    Boolean activo = body.getOrDefault("activo", false);
    productoService.cambiarEstado(id, activo);
  }
}

