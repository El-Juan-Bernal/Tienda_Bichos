package hj.ms_proveedores.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_PROVEEDORES = "proveedores.direct";
    public static final String EXCHANGE_PROVEEDORES_DLX = "proveedores.dead.dlx";

    public static final String QUEUE_ORDENES = "q.proveedores.ordenes";
    public static final String QUEUE_ORDENES_DLQ = "q.proveedores.ordenes.dlq";

    public static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
    public static final String X_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";

    @Bean
    public DirectExchange proveedoresExchange() {
        return new DirectExchange(EXCHANGE_PROVEEDORES);
    }

    @Bean
    public DirectExchange proveedoresDeadLetterExchange() {
        return new DirectExchange(EXCHANGE_PROVEEDORES_DLX);
    }

    @Bean
    public Queue ordenesQueue() {
        return QueueBuilder.durable(QUEUE_ORDENES)
                .withArgument(X_DEAD_LETTER_EXCHANGE, EXCHANGE_PROVEEDORES_DLX)
                .withArgument(X_DEAD_LETTER_ROUTING_KEY, QUEUE_ORDENES_DLQ)
                .build();
    }

    @Bean
    public Queue ordenesDlq() { 
        return QueueBuilder.durable(QUEUE_ORDENES_DLQ).build(); 
    }

    @Bean
    public Binding bindingOrdenesDirect() {
        return BindingBuilder.bind(ordenesQueue()).to(proveedoresExchange()).with("orden.generar");
    }

    @Bean
    public Binding bindingOrdenesDlq() {
        return BindingBuilder.bind(ordenesDlq()).to(proveedoresDeadLetterExchange()).with(QUEUE_ORDENES_DLQ);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}

