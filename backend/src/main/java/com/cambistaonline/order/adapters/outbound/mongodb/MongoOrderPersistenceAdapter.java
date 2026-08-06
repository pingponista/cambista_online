package com.cambistaonline.order.adapters.outbound.mongodb;

import com.cambistaonline.engine.domain.model.CurrencyType;
import com.cambistaonline.engine.domain.model.OperationType;
import com.cambistaonline.order.application.ports.outbound.ExchangeOrderPersistencePort;
import com.cambistaonline.order.domain.model.ExchangeOrder;
import com.cambistaonline.order.domain.model.OrderStatus;
import com.cambistaonline.order.domain.ports.ExchangeOrderRepositoryPort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Primary
public class MongoOrderPersistenceAdapter implements ExchangeOrderRepositoryPort, ExchangeOrderPersistencePort {

    private final SpringDataMongoOrderRepository orderRepository;

    public MongoOrderPersistenceAdapter(SpringDataMongoOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public ExchangeOrder save(ExchangeOrder order) {
        OrderDocument doc = mapToDocument(order);
        OrderDocument saved = orderRepository.save(doc);
        return mapToDomain(saved);
    }

    @Override
    public Optional<ExchangeOrder> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber).map(this::mapToDomain);
    }

    @Override
    public List<ExchangeOrder> findByUserEmail(String userEmail) {
        return orderRepository.findByUserEmailOrderByCreatedAtDesc(userEmail).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    private OrderDocument mapToDocument(ExchangeOrder domain) {
        Optional<OrderDocument> existing = orderRepository.findByOrderNumber(domain.getOrderNumber());
        String mongoId = existing.map(OrderDocument::getMongoId).orElse(null);

        return new OrderDocument(
                mongoId,
                domain.getOrderNumber(),
                domain.getOperationType().name(),
                domain.getCurrencyOrigin().name(),
                domain.getCurrencyDestination().name(),
                domain.getAmountSent(),
                domain.getAmountReceived(),
                domain.getExchangeRate(),
                domain.getPointsRedeemed(),
                domain.getStatus().name(),
                domain.getUserEmail(),
                domain.getUserRole(),
                domain.getExpiresAt(),
                domain.getCreatedAt()
        );
    }

    private ExchangeOrder mapToDomain(OrderDocument doc) {
        return new ExchangeOrder(
                null,
                doc.getOrderNumber(),
                OperationType.valueOf(doc.getOperationType()),
                CurrencyType.valueOf(doc.getCurrencyOrigin()),
                CurrencyType.valueOf(doc.getCurrencyDestination()),
                doc.getAmountSent(),
                doc.getAmountReceived(),
                doc.getExchangeRate(),
                doc.getPointsRedeemed() != null ? doc.getPointsRedeemed() : 0,
                OrderStatus.valueOf(doc.getStatus()),
                doc.getUserEmail(),
                doc.getUserRole(),
                doc.getExpiresAt(),
                doc.getCreatedAt()
        );
    }
}
