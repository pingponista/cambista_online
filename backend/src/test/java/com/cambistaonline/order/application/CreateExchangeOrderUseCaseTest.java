package com.cambistaonline.order.application;

import com.cambistaonline.engine.application.dto.CalculateRateResponse;
import com.cambistaonline.engine.application.usecases.CalculateExchangeRateUseCase;
import com.cambistaonline.engine.domain.model.CurrencyType;
import com.cambistaonline.engine.domain.model.CustomerLevel;
import com.cambistaonline.engine.domain.model.OperationType;
import com.cambistaonline.engine.domain.ports.UserPointsRepositoryPort;
import com.cambistaonline.order.application.dto.CreateOrderRequest;
import com.cambistaonline.order.application.dto.CreateOrderResponse;
import com.cambistaonline.order.application.dto.OrderSummaryDto;
import com.cambistaonline.order.application.usecases.CreateExchangeOrderUseCase;
import com.cambistaonline.order.application.usecases.GetMyOrdersUseCase;
import com.cambistaonline.order.domain.model.ExchangeOrder;
import com.cambistaonline.order.domain.ports.ExchangeOrderRepositoryPort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class CreateExchangeOrderUseCaseTest {

    private ExchangeOrderRepositoryPort orderRepositoryPort;
    private CalculateExchangeRateUseCase calculateExchangeRateUseCase;
    private UserPointsRepositoryPort userPointsRepositoryPort;
    private CreateExchangeOrderUseCase createOrderUseCase;
    private GetMyOrdersUseCase getMyOrdersUseCase;

    @BeforeEach
    void setUp() {
        orderRepositoryPort = Mockito.mock(ExchangeOrderRepositoryPort.class);
        calculateExchangeRateUseCase = Mockito.mock(CalculateExchangeRateUseCase.class);
        userPointsRepositoryPort = Mockito.mock(UserPointsRepositoryPort.class);

        createOrderUseCase = new CreateExchangeOrderUseCase(orderRepositoryPort, calculateExchangeRateUseCase, userPointsRepositoryPort);
        getMyOrdersUseCase = new GetMyOrdersUseCase(orderRepositoryPort);
    }

    @Test
    @DisplayName("Debe crear una nueva orden de cambio correctamente con expiración a 15 minutos")
    void shouldCreateExchangeOrderSuccessfully() {
        // Arrange
        CalculateRateResponse.TipoCambioData rateData = new CalculateRateResponse.TipoCambioData(
                BigDecimal.valueOf(3.7565), BigDecimal.valueOf(0.0100), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(3.7690)
        );
        CalculateRateResponse engineResponse = new CalculateRateResponse(null, rateData, null, List.of(), "00", "OK");

        when(calculateExchangeRateUseCase.execute(any(), eq("demo@cambistaonline.pe"), eq("demo@cambistaonline.pe"), eq("J"), eq(CustomerLevel.PREFERENTE)))
                .thenReturn(engineResponse);

        when(orderRepositoryPort.save(any(ExchangeOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateOrderRequest request = new CreateOrderRequest(
                CurrencyType.USD, CurrencyType.PEN, OperationType.COMPRA, BigDecimal.valueOf(2500.00), 50
        );

        // Act
        CreateOrderResponse response = createOrderUseCase.execute(request, "demo@cambistaonline.pe", "J");

        // Assert
        assertNotNull(response);
        assertTrue(response.getOperationId().startsWith("TRX-"));
        assertEquals("PENDING_PAYMENT", response.getStatus());
        assertEquals(0, new BigDecimal("3.7690").compareTo(response.getExchangeRate()));
        assertEquals(0, new BigDecimal("2500.00").compareTo(response.getAmountSent()));
        assertEquals(0, new BigDecimal("9422.50").compareTo(response.getAmountReceived()));
        assertEquals("REALIZAR_TRANSFERENCIA", response.getNextStep());
        assertNotNull(response.getExpiresAt());
    }

    @Test
    @DisplayName("Debe consultar el historial de ordenes del usuario autenticado")
    void shouldGetMyOrdersSuccessfully() {
        ExchangeOrder order = ExchangeOrder.create(
                "TRX-987654321", OperationType.COMPRA, CurrencyType.USD, CurrencyType.PEN,
                BigDecimal.valueOf(2500.00), BigDecimal.valueOf(3.7690), 50, "demo@cambistaonline.pe", "J"
        );

        when(orderRepositoryPort.findByUserEmail(eq("demo@cambistaonline.pe")))
                .thenReturn(List.of(order));

        List<OrderSummaryDto> orders = getMyOrdersUseCase.execute("demo@cambistaonline.pe");

        assertEquals(1, orders.size());
        assertEquals("TRX-987654321", orders.get(0).getOperationId());
        assertEquals(OperationType.COMPRA, orders.get(0).getOperationType());
        assertEquals("PENDING_PAYMENT", orders.get(0).getStatus());
    }
}
