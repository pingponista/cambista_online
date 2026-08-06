package com.cambistaonline.order.adapters.outbound.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;

@Document(collection = "operacion")
public class OrderDocument {

    @Id
    private String mongoId;

    @Field("order_number")
    private String orderNumber;

    @Field("operation_type")
    private String operationType;

    @Field("currency_origin")
    private String currencyOrigin;

    @Field("currency_destination")
    private String currencyDestination;

    @Field("amount_sent")
    private BigDecimal amountSent;

    @Field("amount_received")
    private BigDecimal amountReceived;

    @Field("exchange_rate")
    private BigDecimal exchangeRate;

    @Field("points_redeemed")
    private Integer pointsRedeemed;

    @Field("status")
    private String status;

    @Field("user_email")
    private String userEmail;

    @Field("user_role")
    private String userRole;

    @Field("expires_at")
    private Object expiresAt;

    @Field("created_at")
    private Object createdAt;

    public OrderDocument() {}

    public OrderDocument(String mongoId, String orderNumber, String operationType, String currencyOrigin, String currencyDestination, BigDecimal amountSent, BigDecimal amountReceived, BigDecimal exchangeRate, Integer pointsRedeemed, String status, String userEmail, String userRole, Object expiresAt, Object createdAt) {
        this.mongoId = mongoId;
        this.orderNumber = orderNumber;
        this.operationType = operationType;
        this.currencyOrigin = currencyOrigin;
        this.currencyDestination = currencyDestination;
        this.amountSent = amountSent;
        this.amountReceived = amountReceived;
        this.exchangeRate = exchangeRate;
        this.pointsRedeemed = pointsRedeemed;
        this.status = status;
        this.userEmail = userEmail;
        this.userRole = userRole;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public String getMongoId() { return mongoId; }
    public void setMongoId(String mongoId) { this.mongoId = mongoId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }

    public String getCurrencyOrigin() { return currencyOrigin; }
    public void setCurrencyOrigin(String currencyOrigin) { this.currencyOrigin = currencyOrigin; }

    public String getCurrencyDestination() { return currencyDestination; }
    public void setCurrencyDestination(String currencyDestination) { this.currencyDestination = currencyDestination; }

    public BigDecimal getAmountSent() { return amountSent; }
    public void setAmountSent(BigDecimal amountSent) { this.amountSent = amountSent; }

    public BigDecimal getAmountReceived() { return amountReceived; }
    public void setAmountReceived(BigDecimal amountReceived) { this.amountReceived = amountReceived; }

    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }

    public Integer getPointsRedeemed() { return pointsRedeemed; }
    public void setPointsRedeemed(Integer pointsRedeemed) { this.pointsRedeemed = pointsRedeemed; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public Object getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Object expiresAt) { this.expiresAt = expiresAt; }

    public Object getCreatedAt() { return createdAt; }
    public void setCreatedAt(Object createdAt) { this.createdAt = createdAt; }
}
