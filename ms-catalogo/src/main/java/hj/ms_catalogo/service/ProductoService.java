package hj.ms_catalogo.service;

import hj.ms_catalogo.config.RabbitMQConfig;
import hj.ms_catalogo.dto.OrdenCompraDto;
import hj.ms_catalogo.dto.ProductoRequest;
import hj.ms_catalogo.dto.ProductoResponse;
import hj.ms_catalogo.dto.StockEventoDto;
import hj.ms_catalogo.entity.MovimientoStock;
import hj.ms_catalogo.entity.Producto;
import hj.ms_catalogo.exception.DestacadoNoPermitidoException;
import hj.ms_catalogo.repository.MovimientoStockRepository;
import hj.ms_catalogo.repository.ProductoRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    // NUEVO: máximo de productos destacados en la página de inicio
    private static final int MAXIMO_DESTACADOS = 8;

    private static final String TIPO_REPOSICION = "REPOSICION_CANCELACION";

    private final ProductoRepository productoRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final RabbitTemplate rabbitTemplate;

    public ProductoService(ProductoRepository productoRepository,
                           MovimientoStockRepository movimientoStockRepository,
                           RabbitTemplate rabbitTemplate) {
        this.productoRepository = productoRepository;
        this.movimientoStockRepository = movimientoStockRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public List<ProductoResponse> obtenerTodos() {
        return productoRepository.findAll().stream()
                .map(this::mapearAResponse)
                .toList();
    }

    // NUEVO
    public List<ProductoResponse> obtenerDestacados() {
        return productoRepository.findDestacadosActivos().stream()
                .limit(MAXIMO_DESTACADOS)
                .map(this::mapearAResponse)
                .toList();
    }

    public ProductoResponse buscarPorId(Long id) {
        return productoRepository.findById(id)
                .map(this::mapearAResponse)
                .orElse(null);
    }

    // CAMBIO: se agrega el campo destacado
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
            p.getActivo() == null || p.getActivo(),
            Boolean.TRUE.equals(p.getDestacado())
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
            aplicarActivo(producto, datosNuevos.activo()); // CAMBIO
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
        aplicarActivo(producto, activo); // CAMBIO
        productoRepository.save(producto);
    }

    // NUEVO: un producto deshabilitado deja de ser destacado
    private void aplicarActivo(Producto producto, boolean activo) {
        producto.setActivo(activo);
        if (!activo && Boolean.TRUE.equals(producto.getDestacado())) {
            producto.setDestacado(false);
            log.info("Producto deshabilitado: se le quitó la estrella de destacado ({})", producto.getNombre());
        }
    }

    // NUEVO: enciende o apaga la estrella, con tope de MAXIMO_DESTACADOS
    @Transactional
    public ProductoResponse cambiarDestacado(Long id, boolean destacado) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(MSG_NO_ENCONTRADO + id));

        boolean yaDestacado = Boolean.TRUE.equals(producto.getDestacado());
        if (destacado && !yaDestacado) {
            if (Boolean.FALSE.equals(producto.getActivo())) {
                throw new DestacadoNoPermitidoException(
                        "No se puede destacar un producto deshabilitado. Habilítalo primero.");
            }
            if (productoRepository.contarDestacadosActivos() >= MAXIMO_DESTACADOS) {
                throw new DestacadoNoPermitidoException(
                        "Ya tienes " + MAXIMO_DESTACADOS
                                + " productos destacados. Quita una estrella para destacar otro.");
            }
        }

        producto.setDestacado(destacado);
        return mapearAResponse(productoRepository.save(producto));
    }

    /**
     * Descuenta stock al aceptar un pedido. Ya NO dispara el reabastecimiento:
     * eso ocurre cuando el pedido sale de bodega (evento PEDIDO_DESPACHADO).
     */
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

    /**
     * Devuelve al stock las unidades de un pedido cancelado. Idempotente: si el mismo pedido
     * ya repuso un producto, no lo repone otra vez. No modifica la bandera de reabastecimiento.
     */
    @Transactional
    public void reponerStockPorCancelacion(Long pedidoId, List<StockEventoDto.ItemStock> items) {
        for (Map.Entry<Long, Integer> entrada : agruparPorProducto(items).entrySet()) {
            Long productoId = entrada.getKey();
            Integer cantidad = entrada.getValue();

            if (movimientoStockRepository.existsByTipoAndPedidoIdAndProductoId(TIPO_REPOSICION, pedidoId, productoId)) {
                log.info("Reposición ya aplicada para pedido #{} y producto {}. Se ignora el mensaje repetido.",
                        pedidoId, productoId);
                continue;
            }

            Producto producto = productoRepository.findById(productoId).orElse(null);
            if (producto == null) {
                log.warn("Reposición omitida: no existe el producto {} (pedido #{})", productoId, pedidoId);
                continue;
            }

            int stockActual = producto.getStock() != null ? producto.getStock() : 0;
            producto.setStock(stockActual + cantidad);
            productoRepository.save(producto);
            movimientoStockRepository.save(new MovimientoStock(TIPO_REPOSICION, pedidoId, productoId, cantidad));

            log.info("Stock repuesto por cancelación del pedido #{}: +{} unidades de {} (stock ahora {})",
                    pedidoId, cantidad, producto.getNombre(), producto.getStock());
        }
    }

    /**
     * Cuando el pedido sale de bodega, se evalúa cada producto: si quedó con poco stock
     * y no hay un reabastecimiento en curso, se solicita la orden de compra al proveedor.
     */
    @Transactional
    public void evaluarReabastecimientoPorDespacho(Long pedidoId, List<StockEventoDto.ItemStock> items) {
        for (Long productoId : agruparPorProducto(items).keySet()) {
            Producto producto = productoRepository.findById(productoId).orElse(null);
            if (producto == null) {
                log.warn("Evaluación omitida: no existe el producto {} (pedido #{})", productoId, pedidoId);
                continue;
            }
            if (solicitarReabastecimientoSiCorresponde(producto)) {
                productoRepository.save(producto);
            }
        }
    }

    private boolean solicitarReabastecimientoSiCorresponde(Producto producto) {
        if (producto.getStock() == null
                || producto.getStock() > UMBRAL_STOCK_BAJO
                || Boolean.TRUE.equals(producto.getReabastecimientoEnProceso())) {
            return false;
        }

        // Bloqueo inmediato para evitar órdenes duplicadas
        producto.setReabastecimientoEnProceso(true);

        OrdenCompraDto orden = new OrdenCompraDto();
        orden.setProductoId(producto.getId());
        orden.setProductoNombre(producto.getNombre());
        orden.setCategoria(producto.getCategoria());
        orden.setCantidadRequerida(50);

        // Envío del evento al broker de proveedores
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_PROVEEDORES, "orden.generar", orden);

        log.info("Alerta de stock bajo disparada ({} unidades). Generando orden B2B para: {}",
                producto.getStock(), producto.getNombre());
        return true;
    }

    private Map<Long, Integer> agruparPorProducto(List<StockEventoDto.ItemStock> items) {
        Map<Long, Integer> agrupado = new LinkedHashMap<>();
        if (items != null) {
            for (StockEventoDto.ItemStock item : items) {
                if (item.productoId() != null && item.cantidad() != null) {
                    agrupado.merge(item.productoId(), item.cantidad(), Integer::sum);
                }
            }
        }
        return agrupado;
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
            // CAMBIO: la estrella nunca se asigna al crear, así no se salta el tope
            p.setDestacado(false);
        });
        List<Producto> productosGuardados = productoRepository.saveAll(productos);
        return productosGuardados.stream()
                .map(this::mapearAResponse)
                .toList();
    }
}

