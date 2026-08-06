package com.cambistaonline.engine.adapters.outbound.mongodb;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataMongoUserPointsRepository extends MongoRepository<UserPointsDocument, String> {
    Optional<UserPointsDocument> findByUserEmail(String userEmail);
}
