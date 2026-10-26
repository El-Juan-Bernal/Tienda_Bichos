package hj.ms_catalogo.service;

import hj.ms_catalogo.dto.ProductoRequest;
import hj.ms_catalogo.dto.ProductoResponse;
import hj.ms_catalogo.entity.Producto;
import hj.ms_catalogo.repository.ProductoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {

    private static final String MSG_NO_ENCONTRADO = "Producto no encontrado con ID: ";

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<ProductoResponse> obtenerTodos() {
        return productoRepository.findAll().stream()
                .map(this::mapearAResponse)
                .toList();
    }

    public ProductoResponse buscarPorId(Long id) {
        return productoRepository.findById(id)
                .map(this::mapearAResponse)
                .orElse(null);
    }

    private ProductoResponse mapearAResponse(Producto p) {
        return new ProductoResponse(
            p.getId(),
            p.getNombre(),
            p.getDescripcion(),
            p.getPrecio(),
            p.getStock(),
            p.getImagenUrl(),
            p.getCategoria(),
            p.getPorcentajeDescuento() != null ? p.getPorcentajeDescuento() : 0,
            p.getActivo() == null || p.getActivo()
        );
    }

    @Transactional
    public ProductoResponse actualizarProducto(Long id, ProductoRequest datosNuevos) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(MSG_NO_ENCONTRADO + id));

        producto.setNombre(datosNuevos.nombre());
        producto.setDescripcion(datosNuevos.descripcion());
        producto.setPrecio(datosNuevos.precio());
        producto.setStock(datosNuevos.stock());
        producto.setImagenUrl(datosNuevos.imagenUrl());
        producto.setCategoria(datosNuevos.categoria());
        
        if (datosNuevos.porcentajeDescuento() != null) {
            producto.setPorcentajeDescuento(datosNuevos.porcentajeDescuento());
        }
        if (datosNuevos.activo() != null) {
            producto.setActivo(datosNuevos.activo());
        }

        Producto actualizado = productoRepository.save(producto);
        return mapearAResponse(actualizado);
    }

    @Transactional
    public void cambiarEstado(Long id, Boolean activo) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(MSG_NO_ENCONTRADO + id));
        producto.setActivo(activo);
        productoRepository.save(producto);
    }

    @Transactional
    public void descontarStock(Long id, Integer cantidad) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(MSG_NO_ENCONTRADO + id));

        if (producto.getStock() < cantidad) {
            throw new IllegalStateException("Stock insuficiente para el producto: " + producto.getNombre());
        }

        producto.setStock(producto.getStock() - cantidad);
        productoRepository.save(producto);
    }

    @Transactional
    public List<ProductoResponse> guardarMasivo(List<Producto> productos) {
        productos.forEach(p -> {
            if (p.getPorcentajeDescuento() == null) {
                p.setPorcentajeDescuento(0);
            }
            if (p.getActivo() == null) {
                p.setActivo(true);
            }
        });
        List<Producto> productosGuardados = productoRepository.saveAll(productos);
        return productosGuardados.stream()
                .map(this::mapearAResponse)
                .toList();
    }
}

