package com.cambistaonline.order.adapters.inbound.rabbitmq;

import com.cambistaonline.order.domain.events.OrderCompletedEvent;
import com.cambistaonline.order.domain.events.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada (Inbound Adapter) que escucha las colas de notificación de RabbitMQ.
 * Simula el servicio de notificaciones: imprime en consola el email que se enviaría al usuario.
 *
 * En un sistema real, aquí se integraría SendGrid, AWS SES, Twilio SMS, etc.
 */
@Component
public class RabbitMQOrderNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQOrderNotificationConsumer.class);

    /**
     * Escucha la cola order.created.queue.
     * RabbitMQ garantiza entrega al menos una vez y elimina el mensaje al procesarlo.
     */
    @RabbitListener(queues = "order.created.queue")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("╔══════════════════════════════════════════════════╗");
        log.info("║  [RABBITMQ NOTIF] 📧 SIMULACIÓN DE EMAIL         ║");
        log.info("╠══════════════════════════════════════════════════╣");
        log.info("║  Para   : {}", event.getUserEmail());
        log.info("║  Asunto : Su orden de cambio fue registrada");
        log.info("║  ------------------------------------------------");
        log.info("║  Estimado cliente,");
        log.info("║  Su orden {} ha sido creada exitosamente.", event.getOrderNumber());
        log.info("║  Operación : {} de {} a {}",
                event.getOperationType(), event.getCurrencyOrigin(), event.getCurrencyDestination());
        log.info("║  Monto enviado   : {} {}", event.getAmountSent(), event.getCurrencyOrigin());
        log.info("║  Monto a recibir : {} {}", event.getAmountReceived(), event.getCurrencyDestination());
        log.info("║  Tasa aplicada   : {}", event.getExchangeRate());
        log.info("║  Por favor realice su transferencia bancaria.");
        log.info("╚══════════════════════════════════════════════════╝");
    }

    /**
     * Escucha la cola order.completed.queue.
     */
    @RabbitListener(queues = "order.completed.queue")
    public void handleOrderCompleted(OrderCompletedEvent event) {
        log.info("╔══════════════════════════════════════════════════╗");
        log.info("║  [RABBITMQ NOTIF] 📧 SIMULACIÓN DE EMAIL         ║");
        log.info("╠══════════════════════════════════════════════════╣");
        log.info("║  Para   : {}", event.getUserEmail());
        log.info("║  Asunto : ¡Su cambio de divisa fue completado!");
        log.info("║  ------------------------------------------------");
        log.info("║  Estimado cliente,");
        log.info("║  Su orden {} ha sido COMPLETADA. ✅", event.getOrderNumber());
        log.info("║  Recibió: {} {}", event.getAmountReceived(), event.getCurrencyDestination());
        log.info("║  Tasa final aplicada: {}", event.getExchangeRate());
        log.info("║  Gracias por usar CambistaOnline.");
        log.info("╚══════════════════════════════════════════════════╝");
    }
}
