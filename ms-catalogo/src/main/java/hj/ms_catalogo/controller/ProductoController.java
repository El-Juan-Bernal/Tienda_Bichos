package hj.ms_catalogo.controller;

import hj.ms_catalogo.dto.ProductoRequest;
import hj.ms_catalogo.dto.ProductoResponse;
import hj.ms_catalogo.entity.Producto;
import hj.ms_catalogo.exception.DestacadoNoPermitidoException;
import hj.ms_catalogo.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Productos", description = "Catálogo de productos, stock y destacados")
public class ProductoController {

    private static final String DESC_ID = "Id del producto";

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @Operation(summary = "Listar todos los productos")
    @GetMapping
    public List<ProductoResponse> listar() {
        return productoService.obtenerTodos();
    }

    @Operation(summary = "Listar los productos destacados",
        description = "Productos activos con la estrella encendida (máximo 8).")
    @GetMapping("/destacados")
    public List<ProductoResponse> destacados() {
        return productoService.obtenerDestacados();
    }

    @Operation(summary = "Buscar un producto por id")
    @GetMapping("/{id}")
    public ProductoResponse buscarPorId(@Parameter(description = DESC_ID) @PathVariable Long id) {
        return productoService.buscarPorId(id);
    }

    @Operation(summary = "Descontar stock",
        description = "Resta la cantidad indicada al stock del producto. Lo usa ms-pedidos al aceptar un pedido.")
    @ApiResponse(responseCode = "200", description = "Stock descontado")
    @PutMapping("/{id}/descontar-stock")
    public ResponseEntity<Void> descontarStock(
            @Parameter(description = DESC_ID) @PathVariable Long id,
            @Parameter(description = "Unidades a descontar") @RequestParam Integer cantidad) {
        productoService.descontarStock(id, cantidad);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Actualizar un producto",
        description = "Reemplaza los datos del producto (precio, stock, descuento, etc.).")
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizarProducto(
            @Parameter(description = DESC_ID) @PathVariable Long id,
            @RequestBody ProductoRequest request) {
        ProductoResponse productoActualizado = productoService.actualizarProducto(id, request);
        return ResponseEntity.ok(productoActualizado);
    }

    @Operation(summary = "Habilitar o deshabilitar un producto",
        description = "Cuerpo: {\"activo\": true} o {\"activo\": false}.")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @PutMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(
            @Parameter(description = DESC_ID) @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        Boolean activo = body.getOrDefault("activo", false);
        productoService.cambiarEstado(id, activo);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Encender o apagar la estrella de destacado",
        description = "Cuerpo: {\"destacado\": true} o {\"destacado\": false}.")
    @ApiResponse(responseCode = "200", description = "Destacado actualizado")
    @ApiResponse(responseCode = "404", description = "El producto no existe")
    @ApiResponse(responseCode = "409", description = "Ya hay 8 destacados o el producto está deshabilitado")
    @PutMapping("/{id}/destacado")
    public ResponseEntity<Object> cambiarDestacado(
            @Parameter(description = DESC_ID) @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        boolean destacado = Boolean.TRUE.equals(body.get("destacado"));
        try {
            return ResponseEntity.ok(productoService.cambiarDestacado(id, destacado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (DestacadoNoPermitidoException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Crear varios productos a la vez")
    @ApiResponse(responseCode = "201", description = "Productos creados")
    @PostMapping("/masivo")
    public ResponseEntity<List<ProductoResponse>> crearProductosMasivamente(@RequestBody List<Producto> productos) {
        List<ProductoResponse> nuevosProductos = productoService.guardarMasivo(productos);
        return new ResponseEntity<>(nuevosProductos, HttpStatus.CREATED);
    }
}

