package com.cambistaonline.order.adapters.outbound.mongodb;

import com.cambistaonline.engine.domain.model.CurrencyType;
import com.cambistaonline.engine.domain.model.OperationType;
import com.cambistaonline.order.application.ports.outbound.ExchangeOrderPersistencePort;
import com.cambistaonline.order.domain.model.ExchangeOrder;
import com.cambistaonline.order.domain.model.OrderStatus;
import com.cambistaonline.order.domain.ports.ExchangeOrderRepositoryPort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
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
        return orderRepository.findByUserEmail(userEmail).stream()
                .map(this::mapToDomain)
                .sorted(Comparator.comparing(ExchangeOrder::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
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
                domain.getExpiresAt() != null ? domain.getExpiresAt() : LocalDateTime.now().plusMinutes(15),
                domain.getCreatedAt() != null ? domain.getCreatedAt() : LocalDateTime.now()
        );
    }

    private ExchangeOrder mapToDomain(OrderDocument doc) {
        String statusStr = doc.getStatus();
        OrderStatus statusEnum;
        try {
            statusEnum = OrderStatus.valueOf(statusStr);
        } catch (Exception e) {
            statusEnum = OrderStatus.COMPLETED;
        }

        String opTypeStr = doc.getOperationType();
        OperationType opTypeEnum = "VENTA".equalsIgnoreCase(opTypeStr) ? OperationType.VENTA : OperationType.COMPRA;

        String currOrigStr = doc.getCurrencyOrigin();
        CurrencyType currOrigEnum = "PEN".equalsIgnoreCase(currOrigStr) ? CurrencyType.PEN : CurrencyType.USD;

        String currDestStr = doc.getCurrencyDestination();
        CurrencyType currDestEnum = "PEN".equalsIgnoreCase(currDestStr) ? CurrencyType.PEN : CurrencyType.USD;

        return new ExchangeOrder(
                null,
                doc.getOrderNumber(),
                opTypeEnum,
                currOrigEnum,
                currDestEnum,
                doc.getAmountSent(),
                doc.getAmountReceived(),
                doc.getExchangeRate(),
                doc.getPointsRedeemed() != null ? doc.getPointsRedeemed() : 0,
                statusEnum,
                doc.getUserEmail(),
                doc.getUserRole(),
                parseDateTime(doc.getExpiresAt()),
                parseDateTime(doc.getCreatedAt())
        );
    }

    private LocalDateTime parseDateTime(Object input) {
        if (input == null) return LocalDateTime.now();
        if (input instanceof LocalDateTime ldt) return ldt;
        if (input instanceof Date date) return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        String str = input.toString().trim();
        try {
            if (str.contains(" ")) {
                str = str.replace(" ", "T");
            }
            if (str.contains("+")) {
                return OffsetDateTime.parse(str).toLocalDateTime();
            }
            return LocalDateTime.parse(str);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
