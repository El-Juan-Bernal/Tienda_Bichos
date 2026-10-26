package hj.ms_pedidos.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchanges
    public static final String EXCHANGE_DIRECT = "cmd.direct";
    public static final String EXCHANGE_TOPIC = "cmd.topic";
    public static final String EXCHANGE_DLX = "cmd.dead.dlx";

    // Colas Principales
    public static final String QUEUE_EMAIL = "q.cmd.email";
    public static final String QUEUE_BODEGA = "q.cmd.bodega";
    public static final String QUEUE_INVOICE = "q.cmd.invoice";

    // Colas DLQ
    public static final String QUEUE_EMAIL_DLQ = "q.cmd.email.dlq";
    public static final String QUEUE_BODEGA_DLQ = "q.cmd.bodega.dlq";
    public static final String QUEUE_INVOICE_DLQ = "q.cmd.invoice.dlq";

    // Constantes para argumentos de RabbitMQ
    public static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
    public static final String X_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";

    // --- EXCHANGES ---
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE_DIRECT);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE_TOPIC);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(EXCHANGE_DLX);
    }

    // --- COLAS PRINCIPALES (Asignando su DLX) ---
    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(QUEUE_EMAIL)
                .withArgument(X_DEAD_LETTER_EXCHANGE, EXCHANGE_DLX)
                .withArgument(X_DEAD_LETTER_ROUTING_KEY, QUEUE_EMAIL_DLQ)
                .build();
    }

    @Bean
    public Queue bodegaQueue() {
        return QueueBuilder.durable(QUEUE_BODEGA)
                .withArgument(X_DEAD_LETTER_EXCHANGE, EXCHANGE_DLX)
                .withArgument(X_DEAD_LETTER_ROUTING_KEY, QUEUE_BODEGA_DLQ)
                .build();
    }

    @Bean
    public Queue invoiceQueue() {
        return QueueBuilder.durable(QUEUE_INVOICE)
                .withArgument(X_DEAD_LETTER_EXCHANGE, EXCHANGE_DLX)
                .withArgument(X_DEAD_LETTER_ROUTING_KEY, QUEUE_INVOICE_DLQ)
                .build();
    }

    // --- COLAS DLQ ---
    @Bean
    public Queue emailDlq() { return QueueBuilder.durable(QUEUE_EMAIL_DLQ).build(); }
    
    @Bean
    public Queue bodegaDlq() { return QueueBuilder.durable(QUEUE_BODEGA_DLQ).build(); }
    
    @Bean
    public Queue invoiceDlq() { return QueueBuilder.durable(QUEUE_INVOICE_DLQ).build(); }

    // --- BINDINGS: EXCHANGE DIRECT ---
    @Bean
    public Binding bindingEmailDirect() {
        return BindingBuilder.bind(emailQueue()).to(directExchange()).with("email.send");
    }

    @Bean
    public Binding bindingBodegaDirect() {
        return BindingBuilder.bind(bodegaQueue()).to(directExchange()).with("bodega.ticket");
    }

    @Bean
    public Binding bindingInvoiceDirect() {
        return BindingBuilder.bind(invoiceQueue()).to(directExchange()).with("invoice.gen");
    }

    // --- BINDINGS: EXCHANGE TOPIC ---
    @Bean
    public Binding bindingEmailTopic() {
        return BindingBuilder.bind(emailQueue()).to(topicExchange()).with("email.*");
    }

    @Bean
    public Binding bindingBodegaTopic() {
        return BindingBuilder.bind(bodegaQueue()).to(topicExchange()).with("bodega.#");
    }

    @Bean
    public Binding bindingInvoiceTopic() {
        return BindingBuilder.bind(invoiceQueue()).to(topicExchange()).with("invoice.*");
    }

    // --- BINDINGS: DLX ---
    @Bean
    public Binding bindingEmailDlq() {
        return BindingBuilder.bind(emailDlq()).to(deadLetterExchange()).with(QUEUE_EMAIL_DLQ);
    }

    @Bean
    public Binding bindingBodegaDlq() {
        return BindingBuilder.bind(bodegaDlq()).to(deadLetterExchange()).with(QUEUE_BODEGA_DLQ);
    }

    @Bean
    public Binding bindingInvoiceDlq() {
        return BindingBuilder.bind(invoiceDlq()).to(deadLetterExchange()).with(QUEUE_INVOICE_DLQ);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}

