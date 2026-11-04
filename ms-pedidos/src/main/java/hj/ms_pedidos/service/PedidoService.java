package hj.ms_pedidos.service;

import hj.ms_pedidos.config.RabbitMQConfig;
import hj.ms_pedidos.dto.CrearPedidoRequest;
import hj.ms_pedidos.dto.ItemEventoDto;
import hj.ms_pedidos.dto.ItemPedidoDto;
import hj.ms_pedidos.dto.PedidoEventoDto;
import hj.ms_pedidos.dto.StockEventoDto;
import hj.ms_pedidos.entity.EstadoPedido;
import hj.ms_pedidos.entity.Pedido;
import hj.ms_pedidos.entity.PedidoHistorial;
import hj.ms_pedidos.entity.PedidoItem;
import hj.ms_pedidos.repository.PedidoHistorialRepository;
import hj.ms_pedidos.repository.PedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    private static final String ROUTING_KEY_EMAIL = "email.send";
    private static final String ROUTING_KEY_BODEGA = "bodega.ticket";
    private static final String ROUTING_KEY_INVOICE = "invoice.gen";

    private static final String ORIGEN_CLIENTE = "CLIENTE";
    private static final String ORIGEN_SISTEMA = "SISTEMA";

    private static final String STOCK_REPOSICION = "REPOSICION_CANCELACION";
    private static final String STOCK_DESPACHADO = "PEDIDO_DESPACHADO";

    private final PedidoRepository pedidoRepository;
    private final PedidoHistorialRepository historialRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RestClient restClient;

    public PedidoService(
            PedidoRepository pedidoRepository,
            PedidoHistorialRepository historialRepository,
            RabbitTemplate rabbitTemplate,
            @Value("${microservices.catalogo.base-url}") String catalogoBaseUrl) {
        this.pedidoRepository = pedidoRepository;
        this.historialRepository = historialRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.restClient = RestClient.builder().baseUrl(catalogoBaseUrl).build();
    }

    @Transactional
    public Pedido crearPedido(CrearPedidoRequest request) {
        Pedido pedido = new Pedido();
        pedido.setUsuarioEmail(request.usuario());
        pedido.setPasarelaSeleccionada(request.pasarelaSeleccionada());
        pedido.setMontoTotal(request.montoTotal());
        pedido.setEstado(EstadoPedido.CREADO);

        List<PedidoItem> itemsEntidad = new ArrayList<>();
        if (request.items() != null) {
            for (ItemPedidoDto dto : request.items()) {
                PedidoItem item = new PedidoItem();
                item.setProductoId(dto.productoId());
                item.setCantidad(dto.cantidad());
                itemsEntidad.add(item);
            }
        }
        pedido.setItems(itemsEntidad);

        Pedido guardado = pedidoRepository.save(pedido);
        historialRepository.save(new PedidoHistorial(guardado.getId(), null, EstadoPedido.CREADO, ORIGEN_CLIENTE));
        publicarEvento(guardado);
        return guardado;
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    public List<Pedido> listarPorUsuario(String email) {
        return pedidoRepository.findByUsuarioEmail(email);
    }

    public List<PedidoHistorial> listarHistorial(Long pedidoId) {
        return historialRepository.findByPedidoIdOrderByFechaAsc(pedidoId);
    }

    /** Cambio de estado pedido desde fuera (por ejemplo, el ACEPTADO que envía el front). */
    @Transactional
    public Pedido actualizarEstado(Long pedidoId, EstadoPedido nuevoEstado) {
        if (nuevoEstado == EstadoPedido.CANCELADO) {
            return cancelarPedido(pedidoId, null);
        }
        Pedido pedido = obtenerBloqueado(pedidoId);
        validarTransicion(pedido.getEstado(), nuevoEstado);
        return aplicarCambio(pedido, nuevoEstado, ORIGEN_CLIENTE);
    }

    /**
     * Avance automático. Devuelve false si el pedido ya no está en el estado esperado
     * (por ejemplo, fue cancelado), para que el avance se detenga sin errores.
     */
    @Transactional
    public boolean avanzarAutomatico(Long pedidoId, EstadoPedido esperado, EstadoPedido siguiente) {
        Pedido pedido = obtenerBloqueado(pedidoId);
        if (pedido.getEstado() != esperado) {
            log.info("Avance automático detenido para pedido #{}: estado actual {} (se esperaba {})",
                    pedidoId, pedido.getEstado(), esperado);
            return false;
        }
        validarTransicion(esperado, siguiente);
        aplicarCambio(pedido, siguiente, ORIGEN_SISTEMA);
        return true;
    }

    /**
     * Cancela el pedido, simula el reembolso y avisa a stock y notificaciones.
     * Si 'solicitante' viene informado, debe coincidir con el dueño del pedido.
     * Es idempotente: cancelar un pedido ya cancelado no repite nada.
     */
    @Transactional
    public Pedido cancelarPedido(Long pedidoId, String solicitante) {
        Pedido pedido = obtenerBloqueado(pedidoId);

        if (solicitante != null && !solicitante.isBlank()
                && !solicitante.equalsIgnoreCase(pedido.getUsuarioEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El pedido no pertenece al usuario.");
        }

        EstadoPedido actual = pedido.getEstado();
        if (actual == EstadoPedido.CANCELADO) {
            return pedido;
        }
        if (actual != EstadoPedido.ACEPTADO && actual != EstadoPedido.EN_PREPARACION) {
            throw new IllegalStateException(mensajeNoCancelable(actual));
        }

        LocalDateTime ahora = LocalDateTime.now(ZoneId.of("America/Santiago"));
        pedido.setEstado(EstadoPedido.CANCELADO);
        pedido.setFechaCancelacion(ahora);
        // Reembolso simulado: se devuelve lo que pagó el cliente por la pasarela elegida
        pedido.setMontoReembolsado(pedido.getMontoTotal() != null ? pedido.getMontoTotal() : 0.0);
        pedido.setFechaReembolso(ahora);

        Pedido guardado = pedidoRepository.save(pedido);
        historialRepository.save(new PedidoHistorial(
                guardado.getId(), actual, EstadoPedido.CANCELADO, ORIGEN_CLIENTE));

        publicarEvento(guardado);
        publicarStock(STOCK_REPOSICION, guardado);
        log.info("Pedido #{} cancelado. Reembolso simulado de {} por {}",
                guardado.getId(), guardado.getMontoReembolsado(), guardado.getPasarelaSeleccionada());
        return guardado;
    }

    private Pedido obtenerBloqueado(Long pedidoId) {
        return pedidoRepository.findByIdForUpdate(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado con ID: " + pedidoId));
    }

    private Pedido aplicarCambio(Pedido pedido, EstadoPedido nuevo, String origen) {
        EstadoPedido anterior = pedido.getEstado();

        // Al pasar a ACEPTADO se descuenta el stock de todos los ítems en ms-catalogo
        if (nuevo == EstadoPedido.ACEPTADO) {
            for (PedidoItem item : pedido.getItems()) {
                restClient.put()
                        .uri(uriBuilder -> uriBuilder
                                .path("/api/productos/{id}/descontar-stock")
                                .queryParam("cantidad", item.getCantidad())
                                .build(item.getProductoId()))
                        .retrieve()
                        .toBodilessEntity();
            }
        }

        pedido.setEstado(nuevo);
        Pedido guardado = pedidoRepository.save(pedido);
        historialRepository.save(new PedidoHistorial(guardado.getId(), anterior, nuevo, origen));
        publicarEvento(guardado);

        // Al salir de bodega, ms-catalogo evalúa si corresponde pedir reabastecimiento
        if (nuevo == EstadoPedido.DESPACHADO) {
            publicarStock(STOCK_DESPACHADO, guardado);
        }
        return guardado;
    }

    private void validarTransicion(EstadoPedido actual, EstadoPedido nuevo) {
        if (actual == EstadoPedido.CANCELADO || actual == EstadoPedido.ENTREGADO) {
            throw new IllegalStateException(
                    "El pedido ya está finalizado en estado " + actual + " y no puede modificarse.");
        }

        boolean transicionValida = switch (actual) {
            case CREADO -> nuevo == EstadoPedido.ACEPTADO;
            case ACEPTADO -> nuevo == EstadoPedido.EN_PREPARACION;
            case EN_PREPARACION -> nuevo == EstadoPedido.DESPACHADO;
            case DESPACHADO -> nuevo == EstadoPedido.ENTREGADO;
            default -> false;
        };

        if (!transicionValida) {
            throw new IllegalStateException(
                    String.format("Transición inválida: No se puede pasar de %s a %s.", actual, nuevo));
        }
    }

    private String mensajeNoCancelable(EstadoPedido actual) {
        if (actual == EstadoPedido.CREADO) {
            return "El pedido aún no ha sido pagado, no hay nada que cancelar.";
        }
        return "El pedido ya salió de bodega (estado " + actual + ") y no puede cancelarse. "
                + "Puede solicitar una devolución cuando lo reciba.";
    }

    private void publicarStock(String tipo, Pedido pedido) {
        List<StockEventoDto.ItemStock> items = pedido.getItems().stream()
                .map(i -> new StockEventoDto.ItemStock(i.getProductoId(), i.getCantidad()))
                .toList();
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_DIRECT,
                RabbitMQConfig.ROUTING_KEY_STOCK,
                new StockEventoDto(tipo, pedido.getId(), items));
    }

    private void publicarEvento(Pedido pedido) {
        List<ItemEventoDto> itemsEvento = new ArrayList<>();
        int subtotalNetoTotal = 0;

        // 1. Consultar al ms-catalogo por CADA ítem para armar la boleta consolidada
        if (pedido.getItems() != null) {
            for (PedidoItem item : pedido.getItems()) {
                Map<?, ?> productoReal = restClient.get()
                        .uri("/api/productos/{id}", item.getProductoId())
                        .retrieve()
                        .body(Map.class);

                String nombreProducto = (productoReal != null && productoReal.get("nombre") != null)
                        ? productoReal.get("nombre").toString()
                        : "Insecto Exótico";
                Integer precioUnitario = (productoReal != null && productoReal.get("precio") != null)
                        ? Integer.parseInt(productoReal.get("precio").toString())
                        : 0;

                int subtotalItem = precioUnitario * item.getCantidad();
                subtotalNetoTotal += subtotalItem;

                itemsEvento.add(new ItemEventoDto(
                        item.getProductoId(),
                        item.getCantidad(),
                        nombreProducto,
                        precioUnitario,
                        subtotalItem
                ));
            }
        }

        // 2. Cálculos globales de la boleta
        Integer iva = (int) (subtotalNetoTotal * 0.19);
        Integer total = subtotalNetoTotal + iva;

        // 3. Evento completo consolidado
        PedidoEventoDto evento = new PedidoEventoDto(
                pedido.getId(),
                pedido.getUsuarioEmail(),
                pedido.getEstado(),
                LocalDateTime.now(ZoneId.of("America/Santiago")),
                itemsEvento,
                subtotalNetoTotal,
                iva,
                total,
                pedido.getPasarelaSeleccionada(),
                pedido.getMontoReembolsado()
        );

        // 4. Correo y bodega siempre
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_DIRECT, ROUTING_KEY_EMAIL, evento);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_DIRECT, ROUTING_KEY_BODEGA, evento);

        // La cola de boletas no aplica a una cancelación
        if (pedido.getEstado() != EstadoPedido.CANCELADO) {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_DIRECT, ROUTING_KEY_INVOICE, evento);
        }
    }
}

