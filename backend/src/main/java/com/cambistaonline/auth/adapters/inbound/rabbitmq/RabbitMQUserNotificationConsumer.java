package com.cambistaonline.auth.adapters.inbound.rabbitmq;

import com.cambistaonline.auth.domain.events.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada (Inbound Adapter) que escucha la cola de bienvenida de RabbitMQ.
 * Simula el envío de un email de bienvenida al nuevo usuario.
 */
@Component
public class RabbitMQUserNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQUserNotificationConsumer.class);

    @RabbitListener(queues = "user.welcome.queue")
    public void handleWelcome(UserRegisteredEvent event) {
        log.info("╔══════════════════════════════════════════════════╗");
        log.info("║  [RABBITMQ NOTIF] 📧 SIMULACIÓN DE EMAIL         ║");
        log.info("╠══════════════════════════════════════════════════╣");
        log.info("║  Para   : {}", event.getEmail());
        log.info("║  Asunto : ¡Bienvenido a CambistaOnline!");
        log.info("║  ------------------------------------------------");
        log.info("║  Hola {} {},", event.getFirstName(), event.getLastName());
        log.info("║  Tu cuenta ha sido creada exitosamente. 🎉");
        log.info("║  Ya puedes acceder y realizar tu primera operación.");
        log.info("║  ¡Bienvenido a la plataforma!");
        log.info("╚══════════════════════════════════════════════════╝");
    }
}
