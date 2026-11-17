package hj.ms_auditoria.listener;

import hj.ms_auditoria.service.AuditoriaService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrdersEventsListener {

    private final AuditoriaService servicio;

    public OrdersEventsListener(AuditoriaService servicio) {
        this.servicio = servicio;
    }

    @KafkaListener(topics = "${kafka.topic.orders-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumir(ConsumerRecord<String, String> registro) {
        servicio.registrar(registro);
    }
}

