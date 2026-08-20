package com.cambistaonline.auth.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ para CambistaOnline.
 *
 * Topología de mensajería declarada:
 *
 *  Exchange: cambista.notifications (tipo Topic)
 *      ├── Binding: "order.created"   → Queue: order.created.queue
 *      ├── Binding: "order.completed" → Queue: order.completed.queue
 *      └── Binding: "user.welcome"    → Queue: user.welcome.queue
 *
 * Los exchanges y colas se crean automáticamente al iniciar la app.
 * Spring AMQP garantiza la idempotencia: si ya existen, los reutiliza.
 */
@Configuration
public class RabbitMQConfig {

    // ─── Exchange ─────────────────────────────────────────────────────────────

    public static final String EXCHANGE = "cambista.notifications";

    // ─── Queues ──────────────────────────────────────────────────────────────

    public static final String QUEUE_ORDER_CREATED   = "order.created.queue";
    public static final String QUEUE_ORDER_COMPLETED = "order.completed.queue";
    public static final String QUEUE_USER_WELCOME    = "user.welcome.queue";

    // ─── Routing Keys ─────────────────────────────────────────────────────────

    public static final String ROUTING_ORDER_CREATED   = "order.created";
    public static final String ROUTING_ORDER_COMPLETED = "order.completed";
    public static final String ROUTING_USER_WELCOME    = "user.welcome";

    // ─── Bean Declarations ────────────────────────────────────────────────────

    @Bean
    public TopicExchange notificationsExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_CREATED).build();
    }

    @Bean
    public Queue orderCompletedQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_COMPLETED).build();
    }

    @Bean
    public Queue userWelcomeQueue() {
        return QueueBuilder.durable(QUEUE_USER_WELCOME).build();
    }

    @Bean
    public Binding bindingOrderCreated(Queue orderCreatedQueue, TopicExchange notificationsExchange) {
        return BindingBuilder.bind(orderCreatedQueue)
                .to(notificationsExchange)
                .with(ROUTING_ORDER_CREATED);
    }

    @Bean
    public Binding bindingOrderCompleted(Queue orderCompletedQueue, TopicExchange notificationsExchange) {
        return BindingBuilder.bind(orderCompletedQueue)
                .to(notificationsExchange)
                .with(ROUTING_ORDER_COMPLETED);
    }

    @Bean
    public Binding bindingUserWelcome(Queue userWelcomeQueue, TopicExchange notificationsExchange) {
        return BindingBuilder.bind(userWelcomeQueue)
                .to(notificationsExchange)
                .with(ROUTING_USER_WELCOME);
    }

    // ─── Serialización JSON ───────────────────────────────────────────────────

    /**
     * Configura Jackson como serializador de mensajes.
     * Todos los objetos publicados en RabbitMQ se convierten a JSON automáticamente.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
