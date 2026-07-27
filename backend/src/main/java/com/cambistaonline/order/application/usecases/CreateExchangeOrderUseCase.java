package com.cambistaonline.order.application.usecases;

import com.cambistaonline.engine.application.dto.CalculateRateRequest;
import com.cambistaonline.engine.application.dto.CalculateRateResponse;
import com.cambistaonline.engine.application.usecases.CalculateExchangeRateUseCase;
import com.cambistaonline.engine.domain.model.CustomerLevel;
import com.cambistaonline.engine.domain.ports.UserPointsRepositoryPort;
import com.cambistaonline.order.application.dto.CreateOrderRequest;
import com.cambistaonline.order.application.dto.CreateOrderResponse;
import com.cambistaonline.order.domain.model.ExchangeOrder;
import com.cambistaonline.order.domain.ports.ExchangeOrderRepositoryPort;

import java.math.BigDecimal;
import java.security.SecureRandom;

public class CreateExchangeOrderUseCase {
    private final ExchangeOrderRepositoryPort orderRepositoryPort;
    private final CalculateExchangeRateUseCase calculateExchangeRateUseCase;
    private final UserPointsRepositoryPort userPointsRepositoryPort;
    private final SecureRandom random = new SecureRandom();

    public CreateExchangeOrderUseCase(ExchangeOrderRepositoryPort orderRepositoryPort,
                                      CalculateExchangeRateUseCase calculateExchangeRateUseCase,
                                      UserPointsRepositoryPort userPointsRepositoryPort) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.calculateExchangeRateUseCase = calculateExchangeRateUseCase;
        this.userPointsRepositoryPort = userPointsRepositoryPort;
    }

    public CreateOrderResponse execute(CreateOrderRequest request, String userEmail, String userRole) {
        // 1. Validate rate and calculate final rate using Rate Engine
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

        // 2. Generate Unique Transaction Order Number (e.g. TRX-987654321)
        String orderNumber = "TRX-" + (100_000_000 + random.nextInt(900_000_000));

        // 3. Create ExchangeOrder domain entity (calculates amountReceived & 15 min expiry)
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

        // 4. Save entity via Domain Port
        ExchangeOrder savedOrder = orderRepositoryPort.save(order);

        // 5. Update user points balance in tb_usuario_puntos (Deduct redeemed points, add earned points e.g. +10 points per order)
        if (userPointsRepositoryPort != null) {
            int pointsEarned = 10;
            userPointsRepositoryPort.updateUserPoints(userEmail, request.getPointsToRedeem(), pointsEarned);
        }

        // 6. Return Application DTO Response
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
