package hj.bff.controller;

import hj.bff.dto.ProductoResponse;
import hj.bff.service.ProductoService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
}
