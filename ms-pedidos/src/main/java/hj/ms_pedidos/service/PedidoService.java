package hj.ms_pedidos.service;

import hj.ms_pedidos.config.RabbitMQConfig;
import hj.ms_pedidos.dto.CrearPedidoRequest;
import hj.ms_pedidos.dto.ItemEventoDto;
import hj.ms_pedidos.dto.ItemPedidoDto;
import hj.ms_pedidos.dto.PedidoEventoDto;
import hj.ms_pedidos.entity.EstadoPedido;
import hj.ms_pedidos.entity.Pedido;
import hj.ms_pedidos.entity.PedidoItem;
import hj.ms_pedidos.repository.PedidoRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RestClient restClient;
    private static final String ROUTING_KEY_EMAIL = "email.send";
    private static final String ROUTING_KEY_BODEGA = "bodega.ticket";
    private static final String ROUTING_KEY_INVOICE = "invoice.gen";

    public PedidoService(
            PedidoRepository pedidoRepository,
            RabbitTemplate rabbitTemplate,
            @Value("${microservices.catalogo.base-url}") String catalogoBaseUrl) {
        this.pedidoRepository = pedidoRepository;
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

        // Agregamos todos los productos del carrito a la entidad Pedido
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
        publicarEvento(guardado);
        return guardado;
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    public List<Pedido> listarPorUsuario(String email) {
        return pedidoRepository.findByUsuarioEmail(email);
    }

    @Transactional
    public Pedido actualizarEstado(Long pedidoId, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado con ID: " + pedidoId));

        EstadoPedido estadoActual = pedido.getEstado();

        validarTransicion(estadoActual, nuevoEstado);

        // Si el pedido pasa a ACEPTADO, ms-pedidos descuenta el stock de TODOS los ítems en ms-catalogo
        if (nuevoEstado == EstadoPedido.ACEPTADO) {
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

        pedido.setEstado(nuevoEstado);
        Pedido guardado = pedidoRepository.save(pedido);
        publicarEvento(guardado);
        return guardado;
    }

    private void validarTransicion(EstadoPedido actual, EstadoPedido nuevo) {
        if (actual == EstadoPedido.CANCELADO || actual == EstadoPedido.ENTREGADO) {
            throw new IllegalStateException("El pedido ya está finalizado en estado " + actual + " y no puede modificarse.");
        }

        if (nuevo == EstadoPedido.CANCELADO) {
            return;
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
                    String.format("Transición inválida: No se puede pasar de %s a %s.", actual, nuevo)
            );
        }
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

        // 3. Armar el evento completo consolidado para RabbitMQ
        PedidoEventoDto evento = new PedidoEventoDto(
                pedido.getId(),
                pedido.getUsuarioEmail(),
                pedido.getEstado(),
                LocalDateTime.now(ZoneId.of("America/Santiago")),
                itemsEvento,
                subtotalNetoTotal,
                iva,
                total
        );

        // 4. Enviar a las 3 colas principales configuradas en RabbitMQConfig
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_DIRECT,
                ROUTING_KEY_EMAIL,
                evento
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_DIRECT,
                ROUTING_KEY_BODEGA,
                evento
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_DIRECT,
                ROUTING_KEY_INVOICE,
                evento
        );
    }
}

