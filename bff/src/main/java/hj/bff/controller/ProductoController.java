package hj.bff.controller;

import hj.bff.dto.ProductoResponse;
import hj.bff.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos",
    description = "Catálogo. Ver: cualquier usuario autenticado. Crear, editar, habilitar y destacar: solo rol Admin.")
public class ProductoController {

  private static final String DESC_ID = "Id del producto";
  private static final String SOLO_ADMIN = "Requiere rol Admin. ";

  private final ProductoService productoService;

  public ProductoController(ProductoService productoService) {
    this.productoService = productoService;
  }

  @Operation(summary = "Listar los productos", description = "Requiere un usuario autenticado.")
  @GetMapping
  public List<ProductoResponse> listar() {
    return productoService.obtenerTodos();
  }

  // Público (sin token): lo usa la página de inicio
  @Operation(summary = "Listar los productos destacados",
      description = "Público: no requiere token. Lo usa la página de inicio (máximo 8).")
  @SecurityRequirements
  @GetMapping("/destacados")
  public List<ProductoResponse> destacados() {
    return productoService.obtenerDestacados();
  }

  @Operation(summary = "Buscar un producto por id", description = "Requiere un usuario autenticado.")
  @GetMapping("/{id}")
  public ProductoResponse buscarPorId(@Parameter(description = DESC_ID) @PathVariable Long id) {
    return productoService.obtenerPorId(id);
  }

  @Operation(summary = "Crear un producto", description = SOLO_ADMIN)
  @PostMapping
  public ProductoResponse crear(@RequestBody ProductoResponse request) {
    return productoService.crear(request);
  }

  @Operation(summary = "Crear varios productos a la vez", description = SOLO_ADMIN)
  @PostMapping("/masivo")
  public List<ProductoResponse> crearMasivo(@RequestBody List<ProductoResponse> requests) {
    return productoService.crearMasivo(requests);
  }

  @Operation(summary = "Actualizar un producto", description = SOLO_ADMIN + "Precio, stock, descuento, etc.")
  @PutMapping("/{id}")
  public ProductoResponse actualizar(
      @Parameter(description = DESC_ID) @PathVariable Long id, @RequestBody ProductoResponse request) {
    return productoService.actualizar(id, request);
  }

  @Operation(summary = "Habilitar o deshabilitar un producto",
      description = SOLO_ADMIN + "Cuerpo: {\"activo\": true} o {\"activo\": false}.")
  @PutMapping("/{id}/estado")
  public void cambiarEstado(
      @Parameter(description = DESC_ID) @PathVariable Long id, @RequestBody Map<String, Boolean> body) {
    Boolean activo = body.getOrDefault("activo", false);
    productoService.cambiarEstado(id, activo);
  }

  // Solo Admin (lo cubre la regla /api/productos/** de SecurityConfig).
  // Reenvía al front el 409 de ms-catalogo con su mensaje.
  @Operation(summary = "Encender o apagar la estrella de destacado",
      description = SOLO_ADMIN + "Cuerpo: {\"destacado\": true} o {\"destacado\": false}.")
  @ApiResponse(responseCode = "200", description = "Destacado actualizado")
  @ApiResponse(responseCode = "409", description = "Ya hay 8 destacados o el producto está deshabilitado")
  @PutMapping("/{id}/destacado")
  public ResponseEntity<Object> cambiarDestacado(
      @Parameter(description = DESC_ID) @PathVariable Long id, @RequestBody Map<String, Boolean> body) {
    try {
      Boolean destacado = Boolean.TRUE.equals(body.get("destacado"));
      return ResponseEntity.ok(productoService.cambiarDestacado(id, destacado));
    } catch (RestClientResponseException e) {
      return ResponseEntity.status(e.getStatusCode())
          .contentType(MediaType.APPLICATION_JSON)
          .body(e.getResponseBodyAsString());
    }
  }
}
