package com.cambistaonline.order.domain.events;

import com.cambistaonline.engine.domain.model.CurrencyType;
import com.cambistaonline.engine.domain.model.OperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento de dominio inmutable publicado cuando se crea una nueva orden de cambio.
 * Es un POJO puro: no tiene anotaciones de Spring, Kafka ni RabbitMQ.
 * El dominio sabe QUÉ pasó; la infraestructura decide cómo publicarlo.
 */
public class OrderCreatedEvent {

    private final String orderNumber;
    private final String userEmail;
    private final OperationType operationType;
    private final CurrencyType currencyOrigin;
    private final CurrencyType currencyDestination;
    private final BigDecimal amountSent;
    private final BigDecimal amountReceived;
    private final BigDecimal exchangeRate;
    private final LocalDateTime occurredAt;

    public OrderCreatedEvent(String orderNumber, String userEmail,
                             OperationType operationType,
                             CurrencyType currencyOrigin, CurrencyType currencyDestination,
                             BigDecimal amountSent, BigDecimal amountReceived,
                             BigDecimal exchangeRate) {
        this.orderNumber = orderNumber;
        this.userEmail = userEmail;
        this.operationType = operationType;
        this.currencyOrigin = currencyOrigin;
        this.currencyDestination = currencyDestination;
        this.amountSent = amountSent;
        this.amountReceived = amountReceived;
        this.exchangeRate = exchangeRate;
        this.occurredAt = LocalDateTime.now();
    }

    public String getOrderNumber()             { return orderNumber; }
    public String getUserEmail()               { return userEmail; }
    public OperationType getOperationType()    { return operationType; }
    public CurrencyType getCurrencyOrigin()    { return currencyOrigin; }
    public CurrencyType getCurrencyDestination(){ return currencyDestination; }
    public BigDecimal getAmountSent()          { return amountSent; }
    public BigDecimal getAmountReceived()      { return amountReceived; }
    public BigDecimal getExchangeRate()        { return exchangeRate; }
    public LocalDateTime getOccurredAt()       { return occurredAt; }
}
