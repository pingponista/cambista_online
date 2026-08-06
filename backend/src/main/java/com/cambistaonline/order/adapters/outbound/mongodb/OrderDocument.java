package com.cambistaonline.order.adapters.outbound.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;

@Document(collection = "operacion")
public class OrderDocument {

    @Id
    private String mongoId;

    // Standard / New fields
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

    // Legacy / Migrated fields (Spanish SQL naming)
    @Field("nro_orden")
    private String nroOrden;

    @Field("tipo_operacion")
    private String tipoOperacion;

    @Field("moneda_origen")
    private String monedaOrigen;

    @Field("moneda_destino")
    private String monedaDestino;

    @Field("monto_origen")
    private Object montoOrigen;

    @Field("monto_destino")
    private Object montoDestino;

    @Field("tasa_final")
    private Object tasaFinal;

    @Field("puntos_canjeados")
    private Integer puntosCanjeados;

    @Field("estado")
    private String estado;

    @Field("correo_user")
    private String correoUser;

    @Field("rol")
    private String rol;

    @Field("fecha_expiracion")
    private Object fechaExpiracion;

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

    public String getOrderNumber() {
        return orderNumber != null ? orderNumber : nroOrden;
    }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getOperationType() {
        return operationType != null ? operationType : tipoOperacion;
    }
    public void setOperationType(String operationType) { this.operationType = operationType; }

    public String getCurrencyOrigin() {
        return currencyOrigin != null ? currencyOrigin : monedaOrigen;
    }
    public void setCurrencyOrigin(String currencyOrigin) { this.currencyOrigin = currencyOrigin; }

    public String getCurrencyDestination() {
        return currencyDestination != null ? currencyDestination : monedaDestino;
    }
    public void setCurrencyDestination(String currencyDestination) { this.currencyDestination = currencyDestination; }

    public BigDecimal getAmountSent() {
        if (amountSent != null) return amountSent;
        if (montoOrigen != null) return parseBigDecimal(montoOrigen);
        return BigDecimal.ZERO;
    }
    public void setAmountSent(BigDecimal amountSent) { this.amountSent = amountSent; }

    public BigDecimal getAmountReceived() {
        if (amountReceived != null) return amountReceived;
        if (montoDestino != null) return parseBigDecimal(montoDestino);
        return BigDecimal.ZERO;
    }
    public void setAmountReceived(BigDecimal amountReceived) { this.amountReceived = amountReceived; }

    public BigDecimal getExchangeRate() {
        if (exchangeRate != null) return exchangeRate;
        if (tasaFinal != null) return parseBigDecimal(tasaFinal);
        return BigDecimal.ZERO;
    }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }

    public Integer getPointsRedeemed() {
        if (pointsRedeemed != null) return pointsRedeemed;
        if (puntosCanjeados != null) return puntosCanjeados;
        return 0;
    }
    public void setPointsRedeemed(Integer pointsRedeemed) { this.pointsRedeemed = pointsRedeemed; }

    public String getStatus() {
        String st = status != null ? status : estado;
        if ("PENDING_PAYMENT".equalsIgnoreCase(st) || "PAYMENT_UPLOADED".equalsIgnoreCase(st)) {
            return "IN_VERIFICATION";
        }
        return st != null ? st : "COMPLETED";
    }
    public void setStatus(String status) { this.status = status; }

    public String getUserEmail() {
        return userEmail != null ? userEmail : correoUser;
    }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserRole() {
        return userRole != null ? userRole : rol;
    }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public Object getExpiresAt() {
        return expiresAt != null ? expiresAt : fechaExpiracion;
    }
    public void setExpiresAt(Object expiresAt) { this.expiresAt = expiresAt; }

    public Object getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Object createdAt) { this.createdAt = createdAt; }

    private BigDecimal parseBigDecimal(Object obj) {
        if (obj == null) return BigDecimal.ZERO;
        if (obj instanceof BigDecimal bd) return bd;
        if (obj instanceof Number num) return BigDecimal.valueOf(num.doubleValue());
        try {
            return new BigDecimal(obj.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
