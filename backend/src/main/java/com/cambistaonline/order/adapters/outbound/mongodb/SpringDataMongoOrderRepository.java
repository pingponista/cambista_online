package com.cambistaonline.order.adapters.outbound.mongodb;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataMongoOrderRepository extends MongoRepository<OrderDocument, String> {
    Optional<OrderDocument> findByOrderNumber(String orderNumber);
    List<OrderDocument> findByUserEmailOrderByCreatedAtDesc(String userEmail);
}
