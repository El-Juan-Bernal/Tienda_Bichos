package hj.ms_catalogo.service;

import hj.ms_catalogo.config.RabbitMQConfig;
import hj.ms_catalogo.dto.OrdenCompraDto;
import hj.ms_catalogo.dto.ProductoRequest;
import hj.ms_catalogo.dto.ProductoResponse;
import hj.ms_catalogo.entity.Producto;
import hj.ms_catalogo.repository.ProductoRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {

    private static final Logger log = LoggerFactory.getLogger(ProductoService.class);
    private static final String MSG_NO_ENCONTRADO = "Producto no encontrado con ID: ";

    // Si el stock queda en este valor o menos, se dispara la orden de compra
    private static final int UMBRAL_STOCK_BAJO = 5;

    private final ProductoRepository productoRepository;
    private final RabbitTemplate rabbitTemplate;

    public ProductoService(ProductoRepository productoRepository, RabbitTemplate rabbitTemplate) {
        this.productoRepository = productoRepository;
        this.rabbitTemplate = rabbitTemplate;
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

        // Si se repuso el stock por sobre el umbral, la orden de compra ya se cumplió:
        // se libera la bandera para que una futura baja de stock vuelva a disparar la alerta.
        if (producto.getStock() != null
                && producto.getStock() > UMBRAL_STOCK_BAJO
                && Boolean.TRUE.equals(producto.getReabastecimientoEnProceso())) {
            producto.setReabastecimientoEnProceso(false);
            log.info("Stock repuesto ({} unidades). Reabastecimiento finalizado para: {}",
                    producto.getStock(), producto.getNombre());
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

        // Validación de inventario B2B para reabastecimiento
        if (producto.getStock() <= UMBRAL_STOCK_BAJO && !Boolean.TRUE.equals(producto.getReabastecimientoEnProceso())) {

            // Bloqueo inmediato para evitar cruces
            producto.setReabastecimientoEnProceso(true);

            OrdenCompraDto orden = new OrdenCompraDto();
            orden.setProductoId(producto.getId());
            orden.setProductoNombre(producto.getNombre());
            orden.setCategoria(producto.getCategoria());
            orden.setCantidadRequerida(50);

            // Envío del evento al broker secundario
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_PROVEEDORES, "orden.generar", orden);

            log.info("Alerta de stock bajo disparada ({} unidades). Generando orden B2B para: {}", producto.getStock(), producto.getNombre());
        }

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

