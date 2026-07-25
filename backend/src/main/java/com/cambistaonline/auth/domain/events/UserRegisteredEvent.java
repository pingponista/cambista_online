package com.cambistaonline.auth.domain.events;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserRegisteredEvent {
    private final UUID userId;
    private final String email;
    private final String role;
    private final LocalDateTime occurredOn;

    public UserRegisteredEvent(UUID userId, String email, String role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.occurredOn = LocalDateTime.now();
    }

    public UUID getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public LocalDateTime getOccurredOn() { return occurredOn; }
}
