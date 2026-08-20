package com.cambistaonline.order.adapters.outbound.rabbitmq;

import com.cambistaonline.engine.domain.model.CurrencyType;
import com.cambistaonline.engine.domain.model.OperationType;
import com.cambistaonline.order.domain.events.OrderCreatedEvent;
import com.cambistaonline.order.domain.events.OrderCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitMQOrderNotificationAdapterTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitMQOrderNotificationAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new RabbitMQOrderNotificationAdapter(rabbitTemplate);
    }

    @Test
    void notifyOrderCreated_shouldSendToCorrectExchangeAndRoutingKey() {
        OrderCreatedEvent event = new OrderCreatedEvent(
                "TRX-111",
                "test@correo.com",
                OperationType.COMPRA,
                CurrencyType.USD,
                CurrencyType.PEN,
                new BigDecimal("50.00"),
                new BigDecimal("185.00"),
                new BigDecimal("3.7000")
        );

        adapter.notifyOrderCreated(event);

        verify(rabbitTemplate).convertAndSend(
                RabbitMQOrderNotificationAdapter.EXCHANGE,
                RabbitMQOrderNotificationAdapter.ROUTING_CREATED,
                event
        );
    }

    @Test
    void notifyOrderCompleted_shouldSendToCorrectExchangeAndRoutingKey() {
        OrderCompletedEvent event = new OrderCompletedEvent(
                "TRX-222",
                "cliente@correo.com",
                OperationType.VENTA,
                CurrencyType.PEN,
                CurrencyType.USD,
                new BigDecimal("185.00"),
                new BigDecimal("50.00"),
                new BigDecimal("3.7000")
        );

        adapter.notifyOrderCompleted(event);

        verify(rabbitTemplate).convertAndSend(
                RabbitMQOrderNotificationAdapter.EXCHANGE,
                RabbitMQOrderNotificationAdapter.ROUTING_COMPLETED,
                event
        );
    }
}
