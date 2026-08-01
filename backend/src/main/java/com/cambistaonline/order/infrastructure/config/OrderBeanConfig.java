package com.cambistaonline.order.infrastructure.config;

import com.cambistaonline.engine.application.ports.inbound.CalculateExchangeRateUseCase;
import com.cambistaonline.engine.domain.ports.UserPointsRepositoryPort;
import com.cambistaonline.order.application.ports.inbound.ConfirmTransferUseCase;
import com.cambistaonline.order.application.ports.inbound.CreateExchangeOrderUseCase;
import com.cambistaonline.order.application.ports.inbound.GetMyOrdersUseCase;
import com.cambistaonline.order.application.service.ConfirmTransferService;
import com.cambistaonline.order.application.service.CreateExchangeOrderService;
import com.cambistaonline.order.application.service.GetMyOrdersService;
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
        return new CreateExchangeOrderService(orderRepositoryPort, calculateExchangeRateUseCase, userPointsRepositoryPort);
    }

    @Bean
    public GetMyOrdersUseCase getMyOrdersUseCase(ExchangeOrderRepositoryPort orderRepositoryPort) {
        return new GetMyOrdersService(orderRepositoryPort);
    }

    @Bean
    public ConfirmTransferUseCase confirmTransferUseCase(ExchangeOrderRepositoryPort orderRepositoryPort) {
        return new ConfirmTransferService(orderRepositoryPort);
    }
}
