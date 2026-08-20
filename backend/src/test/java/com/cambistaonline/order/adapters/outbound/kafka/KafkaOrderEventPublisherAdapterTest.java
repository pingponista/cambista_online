package com.cambistaonline.order.adapters.outbound.kafka;

import com.cambistaonline.engine.domain.model.CurrencyType;
import com.cambistaonline.engine.domain.model.OperationType;
import com.cambistaonline.order.domain.events.OrderCreatedEvent;
import com.cambistaonline.order.domain.events.OrderCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaOrderEventPublisherAdapterTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private KafkaOrderEventPublisherAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new KafkaOrderEventPublisherAdapter(kafkaTemplate);
    }

    @Test
    void publishOrderCreated_shouldSendToCorrectTopic() {
        OrderCreatedEvent event = new OrderCreatedEvent(
                "TRX-123456789",
                "usuario@correo.com",
                OperationType.COMPRA,
                CurrencyType.USD,
                CurrencyType.PEN,
                new BigDecimal("100.00"),
                new BigDecimal("370.00"),
                new BigDecimal("3.7000")
        );

        adapter.publishOrderCreated(event);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(
                eq(KafkaOrderEventPublisherAdapter.TOPIC_ORDER_CREATED),
                eq("TRX-123456789"),
                payloadCaptor.capture()
        );

        OrderCreatedEvent capturedEvent = (OrderCreatedEvent) payloadCaptor.getValue();
        assertThat(capturedEvent.getOrderNumber()).isEqualTo("TRX-123456789");
        assertThat(capturedEvent.getUserEmail()).isEqualTo("usuario@correo.com");
        assertThat(capturedEvent.getOperationType()).isEqualTo(OperationType.COMPRA);
    }

    @Test
    void publishOrderCompleted_shouldSendToCorrectTopic() {
        OrderCompletedEvent event = new OrderCompletedEvent(
                "TRX-987654321",
                "cliente@correo.com",
                OperationType.VENTA,
                CurrencyType.PEN,
                CurrencyType.USD,
                new BigDecimal("370.00"),
                new BigDecimal("100.00"),
                new BigDecimal("3.7000")
        );

        adapter.publishOrderCompleted(event);

        verify(kafkaTemplate).send(
                eq(KafkaOrderEventPublisherAdapter.TOPIC_ORDER_COMPLETED),
                eq("TRX-987654321"),
                eq(event)
        );
    }
}
