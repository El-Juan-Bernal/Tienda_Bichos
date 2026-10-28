package hj.ms_notificaciones.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    private static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
    private static final String X_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";
    private static final String DEAD_LETTER_EXCHANGE_NAME = "cmd.dead.dlx";

    @Bean
    public Queue emailDlq() {
        return QueueBuilder.durable("q.cmd.email.dlq").build();
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable("q.cmd.email")
                .withArgument(X_DEAD_LETTER_EXCHANGE, DEAD_LETTER_EXCHANGE_NAME)
                .withArgument(X_DEAD_LETTER_ROUTING_KEY, "q.cmd.email.dlq")
                .build();
    }

    @Bean
    public Queue bodegaDlq() {
        return QueueBuilder.durable("q.cmd.bodega.dlq").build();
    }

    @Bean
    public Queue bodegaQueue() {
        return QueueBuilder.durable("q.cmd.bodega")
                .withArgument(X_DEAD_LETTER_EXCHANGE, DEAD_LETTER_EXCHANGE_NAME)
                .withArgument(X_DEAD_LETTER_ROUTING_KEY, "q.cmd.bodega.dlq")
                .build();
    }

    @Bean
    public Queue invoiceDlq() {
        return QueueBuilder.durable("q.cmd.invoice.dlq").build();
    }

    @Bean
    public Queue invoiceQueue() {
        return QueueBuilder.durable("q.cmd.invoice")
                .withArgument(X_DEAD_LETTER_EXCHANGE, DEAD_LETTER_EXCHANGE_NAME)
                .withArgument(X_DEAD_LETTER_ROUTING_KEY, "q.cmd.invoice.dlq")
                .build();
    }
}

