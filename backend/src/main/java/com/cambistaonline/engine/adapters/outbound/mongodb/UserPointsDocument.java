package com.cambistaonline.engine.adapters.outbound.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "usuarios_puntos")
public class UserPointsDocument {

    @Id
    private String mongoId;

    @Field("user_email")
    private String userEmail;

    @Field("saldo_puntos")
    private Integer saldoPuntos;

    @Field("puntos_acumulados")
    private Integer puntosAcumulados;

    public UserPointsDocument() {}

    public UserPointsDocument(String mongoId, String userEmail, Integer saldoPuntos, Integer puntosAcumulados) {
        this.mongoId = mongoId;
        this.userEmail = userEmail;
        this.saldoPuntos = saldoPuntos;
        this.puntosAcumulados = puntosAcumulados;
    }

    public String getMongoId() { return mongoId; }
    public void setMongoId(String mongoId) { this.mongoId = mongoId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public Integer getSaldoPuntos() { return saldoPuntos; }
    public void setSaldoPuntos(Integer saldoPuntos) { this.saldoPuntos = saldoPuntos; }

    public Integer getPuntosAcumulados() { return puntosAcumulados; }
    public void setPuntosAcumulados(Integer puntosAcumulados) { this.puntosAcumulados = puntosAcumulados; }
}
