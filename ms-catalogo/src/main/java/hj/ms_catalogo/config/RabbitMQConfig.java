package hj.ms_catalogo.config;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_PROVEEDORES = "proveedores.direct";
    public static final String QUEUE_STOCK = "q.cmd.stock";
    public static final String PRINCIPAL_LISTENER_FACTORY = "principalListenerFactory";

    // Conexión PRIMARIA: broker de proveedores (la usa el RabbitTemplate para publicar órdenes de compra)
    @Bean
    @Primary
    public ConnectionFactory proveedoresConnectionFactory(
            @Value("${spring.rabbitmq.host}") String host,
            @Value("${spring.rabbitmq.port}") int port,
            @Value("${spring.rabbitmq.username}") String username,
            @Value("${spring.rabbitmq.password}") String password) {
        CachingConnectionFactory cf = new CachingConnectionFactory(host, port);
        cf.setUsername(username);
        cf.setPassword(password);
        return cf;
    }

    // Conexión al broker PRINCIPAL (pedidos): de aquí se consume q.cmd.stock
    @Bean
    public ConnectionFactory principalConnectionFactory(
            @Value("${rabbit-principal.host}") String host,
            @Value("${rabbit-principal.port}") int port,
            @Value("${spring.rabbitmq.username}") String username,
            @Value("${spring.rabbitmq.password}") String password) {
        CachingConnectionFactory cf = new CachingConnectionFactory(host, port);
        cf.setUsername(username);
        cf.setPassword(password);
        return cf;
    }

    @Bean(name = PRINCIPAL_LISTENER_FACTORY)
    public SimpleRabbitListenerContainerFactory principalListenerFactory(
            @Qualifier("principalConnectionFactory") ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        // Si falla, el mensaje va a la DLQ (q.cmd.stock.dlq) en vez de reintentarse sin fin
        factory.setDefaultRequeueRejected(false);
        // Si la cola aún no existe (la crea ms-pedidos), reintenta en vez de fallar al arrancar
        factory.setMissingQueuesFatal(false);
        return factory;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}

