package hj.ms_proveedores.listener;

import hj.ms_proveedores.config.RabbitMQConfig;
import hj.ms_proveedores.dto.OrdenCompraDto;
import hj.ms_proveedores.service.ProveedorService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrdenCompraListener {

    private final ProveedorService proveedorService;

    public OrdenCompraListener(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ORDENES)
    public void recibirOrdenCompra(OrdenCompraDto orden) {
        proveedorService.procesarOrden(orden);
    }
}

