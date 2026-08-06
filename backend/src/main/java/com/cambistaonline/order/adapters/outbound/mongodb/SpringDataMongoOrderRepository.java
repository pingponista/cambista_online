package com.cambistaonline.order.adapters.outbound.mongodb;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataMongoOrderRepository extends MongoRepository<OrderDocument, String> {

    @Query("{ '$or': [ { 'order_number': ?0 }, { 'nro_orden': ?0 } ] }")
    Optional<OrderDocument> findByOrderNumber(String orderNumber);

    @Query("{ '$or': [ { 'user_email': ?0 }, { 'correo_user': ?0 } ] }")
    List<OrderDocument> findByUserEmail(String userEmail);
}
