package hj.ms_catalogo.listener;

import hj.ms_catalogo.config.RabbitMQConfig;
import hj.ms_catalogo.dto.StockEventoDto;
import hj.ms_catalogo.service.ProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class StockListener {

    private static final Logger log = LoggerFactory.getLogger(StockListener.class);

    private final ProductoService productoService;

    public StockListener(ProductoService productoService) {
        this.productoService = productoService;
    }

    @RabbitListener(
            queues = RabbitMQConfig.QUEUE_STOCK,
            containerFactory = RabbitMQConfig.PRINCIPAL_LISTENER_FACTORY)
    public void procesar(StockEventoDto evento) {
        if (evento == null || evento.tipo() == null || evento.items() == null) {
            log.warn("Mensaje de stock inválido: {}", evento);
            return;
        }

        log.info("Recibido en q.cmd.stock: {} del pedido #{}", evento.tipo(), evento.pedidoId());

        switch (evento.tipo()) {
            case "REPOSICION_CANCELACION" ->
                    productoService.reponerStockPorCancelacion(evento.pedidoId(), evento.items());
            case "PEDIDO_DESPACHADO" ->
                    productoService.evaluarReabastecimientoPorDespacho(evento.pedidoId(), evento.items());
            default -> log.warn("Tipo de evento de stock desconocido: {}", evento.tipo());
        }
    }
}

