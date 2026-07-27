package com.cambistaonline.order.application.usecases;

import com.cambistaonline.order.application.dto.OrderSummaryDto;
import com.cambistaonline.order.domain.model.ExchangeOrder;
import com.cambistaonline.order.domain.ports.ExchangeOrderRepositoryPort;

import java.util.Optional;

public class ConfirmTransferUseCase {
    private final ExchangeOrderRepositoryPort orderRepositoryPort;

    public ConfirmTransferUseCase(ExchangeOrderRepositoryPort orderRepositoryPort) {
        this.orderRepositoryPort = orderRepositoryPort;
    }

    public OrderSummaryDto execute(String orderNumber, String userEmail, String transactionNumber) {
        Optional<ExchangeOrder> optionalOrder = orderRepositoryPort.findByOrderNumber(orderNumber);

        if (optionalOrder.isEmpty()) {
            throw new IllegalArgumentException("ORDER_NOT_FOUND: Orden de cambio no encontrada para " + orderNumber);
        }

        ExchangeOrder order = optionalOrder.get();
        order.markAsUploaded();

        ExchangeOrder saved = orderRepositoryPort.save(order);

        return new OrderSummaryDto(
                saved.getOrderNumber(),
                saved.getOperationType(),
                saved.getCurrencyOrigin(),
                saved.getCurrencyDestination(),
                saved.getAmountSent(),
                saved.getAmountReceived(),
                saved.getExchangeRate(),
                saved.getPointsRedeemed(),
                saved.getStatus().name(),
                saved.getCreatedAt(),
                saved.getExpiresAt()
        );
    }
}
