package com.cambistaonline.auth.domain.events;

import java.time.LocalDateTime;

/**
 * Evento de dominio inmutable publicado cuando un nuevo usuario
 * se registra exitosamente en la plataforma.
 */
public class UserRegisteredEvent {

    private final String userId;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final String role;
    private final LocalDateTime occurredAt;

    public UserRegisteredEvent(String userId, String email,
                               String firstName, String lastName, String role) {
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.occurredAt = LocalDateTime.now();
    }

    public String getUserId()          { return userId; }
    public String getEmail()           { return email; }
    public String getFirstName()       { return firstName; }
    public String getLastName()        { return lastName; }
    public String getRole()            { return role; }
    public LocalDateTime getOccurredAt(){ return occurredAt; }
}
