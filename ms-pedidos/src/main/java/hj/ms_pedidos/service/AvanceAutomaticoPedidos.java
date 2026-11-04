package hj.ms_pedidos.service;

import hj.ms_pedidos.entity.EstadoPedido;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class AvanceAutomaticoPedidos {

    private static final Logger log = LoggerFactory.getLogger(AvanceAutomaticoPedidos.class);

    private final PedidoService pedidoService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    private final long esperaPreparacion;
    private final long esperaDespacho;
    private final long esperaEntrega;

    public AvanceAutomaticoPedidos(
            PedidoService pedidoService,
            @Value("${pedidos.avance.espera-preparacion-segundos}") long esperaPreparacion,
            @Value("${pedidos.avance.espera-despacho-segundos}") long esperaDespacho,
            @Value("${pedidos.avance.espera-entrega-segundos}") long esperaEntrega) {
        this.pedidoService = pedidoService;
        this.esperaPreparacion = esperaPreparacion;
        this.esperaDespacho = esperaDespacho;
        this.esperaEntrega = esperaEntrega;
    }

    /** Se llama cuando el pedido pasa a ACEPTADO. */
    public void iniciar(Long pedidoId) {
        programar(pedidoId, EstadoPedido.ACEPTADO, EstadoPedido.EN_PREPARACION, esperaPreparacion);
    }

    private void programar(Long pedidoId, EstadoPedido esperado, EstadoPedido siguiente, long esperaSegundos) {
        scheduler.schedule(() -> {
            try {
                if (!pedidoService.avanzarAutomatico(pedidoId, esperado, siguiente)) {
                    return; // cancelado u otro cambio: se detiene el avance
                }
                switch (siguiente) {
                    case EN_PREPARACION ->
                            programar(pedidoId, EstadoPedido.EN_PREPARACION, EstadoPedido.DESPACHADO, esperaDespacho);
                    case DESPACHADO ->
                            programar(pedidoId, EstadoPedido.DESPACHADO, EstadoPedido.ENTREGADO, esperaEntrega);
                    default -> { /* ENTREGADO es el final */ }
                }
            } catch (Exception e) {
                log.error("Deteniendo avance automático para pedido #{}: {}", pedidoId, e.getMessage());
            }
        }, esperaSegundos, TimeUnit.SECONDS);
    }

    @PreDestroy
    void detener() {
        scheduler.shutdownNow();
    }
}

