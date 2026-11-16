package hj.ms_pedidos.service;

import hj.ms_pedidos.dto.PedidoEventoKafka;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.json.JsonMapper;

/** Publica en Kafka sin afectar el flujo del pedido: si falla, solo avisa en el log. */
@Component
public class PedidoEventosKafka {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventosKafka.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;
    private final String topico;

    public PedidoEventosKafka(KafkaTemplate<String, String> kafkaTemplate,
                              JsonMapper jsonMapper,
                              @Value("${kafka.topic.orders-events}") String topico) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = jsonMapper;
        this.topico = topico;
    }

    /** Si hay una transacción en curso, envía solo cuando se confirma (commit). */
    public void publicar(PedidoEventoKafka evento) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    enviar(evento);
                }
            });
        } else {
            enviar(evento);
        }
    }

    private void enviar(PedidoEventoKafka evento) {
        try {
            String json = jsonMapper.writeValueAsString(evento);
            kafkaTemplate.send(topico, String.valueOf(evento.pedidoId()), json)
                    .whenComplete((resultado, error) -> {
                        if (error != null) {
                            log.warn("No se pudo publicar {} del pedido #{} en Kafka: {}",
                                    evento.type(), evento.pedidoId(), error.getMessage());
                        } else {
                            var meta = resultado.getRecordMetadata();
                            log.info("Kafka {} pedido #{} -> {}-{} offset {}",
                                    evento.type(), evento.pedidoId(), meta.topic(), meta.partition(), meta.offset());
                        }
                    });
        } catch (Exception e) {
            log.warn("No se pudo publicar {} del pedido #{} en Kafka: {}",
                    evento.type(), evento.pedidoId(), e.getMessage());
        }
    }
}

