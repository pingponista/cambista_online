package com.cambistaonline.engine.adapters.outbound.mongodb;

import com.cambistaonline.engine.domain.model.CurrencyType;
import com.cambistaonline.engine.domain.model.CustomerLevel;
import com.cambistaonline.engine.domain.model.OperationType;
import com.cambistaonline.engine.domain.ports.ExchangeRateRepositoryPort;
import com.cambistaonline.engine.domain.ports.ExchangeRuleRepositoryPort;
import com.cambistaonline.engine.domain.ports.UserPointsRepositoryPort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Component
@Primary
public class MongoEnginePersistenceAdapter implements ExchangeRateRepositoryPort, ExchangeRuleRepositoryPort, UserPointsRepositoryPort {

    private final SpringDataMongoUserPointsRepository userPointsRepository;

    public MongoEnginePersistenceAdapter(SpringDataMongoUserPointsRepository userPointsRepository) {
        this.userPointsRepository = userPointsRepository;
    }

    @Override
    public Optional<BigDecimal> findActiveBaseRate(CurrencyType origin, CurrencyType destination, OperationType operation) {
        if (operation == OperationType.COMPRA) {
            return Optional.of(new BigDecimal("3.7450"));
        } else {
            return Optional.of(new BigDecimal("3.7750"));
        }
    }

    @Override
    public Optional<BigDecimal> findSpread(String userRole, CustomerLevel level, OperationType operation) {
        if ("J".equalsIgnoreCase(userRole)) {
            return Optional.of(new BigDecimal("0.0080"));
        } else {
            return Optional.of(new BigDecimal("0.0120"));
        }
    }

    @Override
    public Optional<BigDecimal> findHourlyAdjustment(String userRole, LocalTime time, OperationType operation) {
        LocalTime startHighVolume = LocalTime.of(9, 0);
        LocalTime endHighVolume = LocalTime.of(13, 30);

        if (time.isAfter(startHighVolume) && time.isBefore(endHighVolume)) {
            return Optional.of(new BigDecimal("-0.0015"));
        }
        return Optional.of(BigDecimal.ZERO);
    }

    @Override
    public Optional<BigDecimal> findSeasonalAdjustment(String userRole, LocalDateTime dateTime, OperationType operation) {
        return Optional.of(new BigDecimal("-0.0010"));
    }

    @Override
    public int getUserPointsBalance(String userEmail) {
        return userPointsRepository.findByUserEmail(userEmail)
                .map(UserPointsDocument::getSaldoPuntos)
                .orElse(340);
    }

    @Override
    public Optional<PointsConfig> findPointsConfig(String userRole) {
        return Optional.of(new PointsConfig(100, 100, new BigDecimal("0.0010")));
    }

    @Override
    public void updateUserPoints(String userEmail, int pointsDeducted, int pointsEarned) {
        UserPointsDocument doc = userPointsRepository.findByUserEmail(userEmail)
                .orElseGet(() -> {
                    UserPointsDocument newDoc = new UserPointsDocument();
                    newDoc.setUserEmail(userEmail);
                    newDoc.setSaldoPuntos(340);
                    newDoc.setPuntosAcumulados(1520);
                    return newDoc;
                });

        int currentSaldo = doc.getSaldoPuntos() != null ? doc.getSaldoPuntos() : 340;
        int currentAcumulados = doc.getPuntosAcumulados() != null ? doc.getPuntosAcumulados() : 1520;

        int newSaldo = Math.max(0, currentSaldo - pointsDeducted + pointsEarned);
        int newAcumulados = currentAcumulados + pointsEarned;

        doc.setSaldoPuntos(newSaldo);
        doc.setPuntosAcumulados(newAcumulados);

        userPointsRepository.save(doc);
    }
}
