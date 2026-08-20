package com.cambistaonline.order.application.service;

import com.cambistaonline.engine.application.dto.CalculateRateRequest;
import com.cambistaonline.engine.application.dto.CalculateRateResponse;
import com.cambistaonline.engine.application.ports.inbound.CalculateExchangeRateUseCase;
import com.cambistaonline.engine.domain.model.CustomerLevel;
import com.cambistaonline.engine.domain.ports.UserPointsRepositoryPort;
import com.cambistaonline.order.application.dto.CreateOrderRequest;
import com.cambistaonline.order.application.dto.CreateOrderResponse;
import com.cambistaonline.order.application.ports.inbound.CreateExchangeOrderUseCase;
import com.cambistaonline.order.application.ports.outbound.OrderEventPublisherPort;
import com.cambistaonline.order.application.ports.outbound.OrderNotificationPort;
import com.cambistaonline.order.domain.events.OrderCreatedEvent;
import com.cambistaonline.order.domain.model.ExchangeOrder;
import com.cambistaonline.order.domain.ports.ExchangeOrderRepositoryPort;

import java.math.BigDecimal;
import java.security.SecureRandom;

public class CreateExchangeOrderService implements CreateExchangeOrderUseCase {

    private final ExchangeOrderRepositoryPort orderRepositoryPort;
    private final CalculateExchangeRateUseCase calculateExchangeRateUseCase;
    private final UserPointsRepositoryPort userPointsRepositoryPort;
    private final OrderEventPublisherPort orderEventPublisherPort;
    private final OrderNotificationPort orderNotificationPort;
    private final SecureRandom random = new SecureRandom();

    public CreateExchangeOrderService(ExchangeOrderRepositoryPort orderRepositoryPort,
                                      CalculateExchangeRateUseCase calculateExchangeRateUseCase,
                                      UserPointsRepositoryPort userPointsRepositoryPort,
                                      OrderEventPublisherPort orderEventPublisherPort,
                                      OrderNotificationPort orderNotificationPort) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.calculateExchangeRateUseCase = calculateExchangeRateUseCase;
        this.userPointsRepositoryPort = userPointsRepositoryPort;
        this.orderEventPublisherPort = orderEventPublisherPort;
        this.orderNotificationPort = orderNotificationPort;
    }

    @Override
    public CreateOrderResponse execute(CreateOrderRequest request, String userEmail, String userRole) {
        CalculateRateRequest engineReq = new CalculateRateRequest(
                request.getCurrencyOrigin(),
                request.getCurrencyDestination(),
                request.getOperationType(),
                request.getPointsToRedeem()
        );

        CalculateRateResponse engineRes = calculateExchangeRateUseCase.execute(
                engineReq,
                userEmail,
                userEmail,
                userRole,
                CustomerLevel.PREFERENTE
        );

        BigDecimal finalRate = engineRes.getTipoCambio().getTipoCambioFinal();
        String orderNumber = "TRX-" + (100_000_000 + random.nextInt(900_000_000));

        ExchangeOrder order = ExchangeOrder.create(
                orderNumber,
                request.getOperationType(),
                request.getCurrencyOrigin(),
                request.getCurrencyDestination(),
                request.getAmountSent(),
                finalRate,
                request.getPointsToRedeem(),
                userEmail,
                userRole
        );

        ExchangeOrder savedOrder = orderRepositoryPort.save(order);

        if (userPointsRepositoryPort != null) {
            int pointsEarned = 10;
            userPointsRepositoryPort.updateUserPoints(userEmail, request.getPointsToRedeem(), pointsEarned);
        }

        // ── Publicar evento de dominio → Kafka (auditoría inmutable)
        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getOrderNumber(),
                savedOrder.getUserEmail(),
                savedOrder.getOperationType(),
                savedOrder.getCurrencyOrigin(),
                savedOrder.getCurrencyDestination(),
                savedOrder.getAmountSent(),
                savedOrder.getAmountReceived(),
                savedOrder.getExchangeRate()
        );
        orderEventPublisherPort.publishOrderCreated(event);

        // ── Enviar notificación → RabbitMQ (email simulado al cliente)
        orderNotificationPort.notifyOrderCreated(event);

        return new CreateOrderResponse(
                savedOrder.getOrderNumber(),
                savedOrder.getStatus().name(),
                savedOrder.getExchangeRate(),
                savedOrder.getAmountSent(),
                savedOrder.getAmountReceived(),
                savedOrder.getExpiresAt(),
                "REALIZAR_TRANSFERENCIA"
        );
    }
}
