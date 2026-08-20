package com.cambistaonline.auth.adapters.outbound.rabbitmq;

import com.cambistaonline.auth.application.ports.outbound.UserNotificationPort;
import com.cambistaonline.auth.domain.events.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida (Outbound Adapter) que implementa UserNotificationPort.
 * Envía mensajes de bienvenida al Exchange "cambista.notifications".
 */
@Component
public class RabbitMQUserNotificationAdapter implements UserNotificationPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQUserNotificationAdapter.class);

    static final String EXCHANGE        = "cambista.notifications";
    static final String ROUTING_WELCOME = "user.welcome";

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQUserNotificationAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void notifyWelcome(UserRegisteredEvent event) {
        log.info("[RABBITMQ] Enviando bienvenida → exchange={} | routing={} | email={}",
                EXCHANGE, ROUTING_WELCOME, event.getEmail());
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_WELCOME, event);
    }
}
