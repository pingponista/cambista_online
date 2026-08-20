package com.cambistaonline.order.adapters.outbound.rabbitmq;

import com.cambistaonline.order.application.ports.outbound.OrderNotificationPort;
import com.cambistaonline.order.domain.events.OrderCompletedEvent;
import com.cambistaonline.order.domain.events.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida (Outbound Adapter) que implementa OrderNotificationPort.
 * Es el único lugar del proyecto que conoce RabbitMQ para notificaciones de orden.
 * Envía mensajes al Exchange "cambista.notifications" con routing keys específicas.
 */
@Component
public class RabbitMQOrderNotificationAdapter implements OrderNotificationPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQOrderNotificationAdapter.class);

    static final String EXCHANGE          = "cambista.notifications";
    static final String ROUTING_CREATED   = "order.created";
    static final String ROUTING_COMPLETED = "order.completed";

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQOrderNotificationAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void notifyOrderCreated(OrderCreatedEvent event) {
        log.info("[RABBITMQ] Enviando notificación → exchange={} | routing={} | orden={}",
                EXCHANGE, ROUTING_CREATED, event.getOrderNumber());
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_CREATED, event);
    }

    @Override
    public void notifyOrderCompleted(OrderCompletedEvent event) {
        log.info("[RABBITMQ] Enviando notificación → exchange={} | routing={} | orden={}",
                EXCHANGE, ROUTING_COMPLETED, event.getOrderNumber());
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_COMPLETED, event);
    }
}
