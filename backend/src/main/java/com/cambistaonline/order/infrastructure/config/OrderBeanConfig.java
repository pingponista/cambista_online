package com.cambistaonline.order.infrastructure.config;

import com.cambistaonline.engine.application.usecases.CalculateExchangeRateUseCase;
import com.cambistaonline.engine.domain.ports.UserPointsRepositoryPort;
import com.cambistaonline.order.application.usecases.ConfirmTransferUseCase;
import com.cambistaonline.order.application.usecases.CreateExchangeOrderUseCase;
import com.cambistaonline.order.application.usecases.GetMyOrdersUseCase;
import com.cambistaonline.order.domain.ports.ExchangeOrderRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderBeanConfig {

    @Bean
    public CreateExchangeOrderUseCase createExchangeOrderUseCase(
            ExchangeOrderRepositoryPort orderRepositoryPort,
            CalculateExchangeRateUseCase calculateExchangeRateUseCase,
            UserPointsRepositoryPort userPointsRepositoryPort) {
        return new CreateExchangeOrderUseCase(orderRepositoryPort, calculateExchangeRateUseCase, userPointsRepositoryPort);
    }

    @Bean
    public GetMyOrdersUseCase getMyOrdersUseCase(ExchangeOrderRepositoryPort orderRepositoryPort) {
        return new GetMyOrdersUseCase(orderRepositoryPort);
    }

    @Bean
    public ConfirmTransferUseCase confirmTransferUseCase(ExchangeOrderRepositoryPort orderRepositoryPort) {
        return new ConfirmTransferUseCase(orderRepositoryPort);
    }
}
