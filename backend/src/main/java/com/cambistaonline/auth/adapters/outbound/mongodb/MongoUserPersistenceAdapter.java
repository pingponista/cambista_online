package com.cambistaonline.auth.adapters.outbound.mongodb;

import com.cambistaonline.auth.application.ports.outbound.UserPersistencePort;
import com.cambistaonline.auth.domain.model.User;
import com.cambistaonline.auth.domain.model.UserStatus;
import com.cambistaonline.auth.domain.ports.UserRepositoryPort;
import com.cambistaonline.auth.domain.valueobjects.Dni;
import com.cambistaonline.auth.domain.valueobjects.Email;
import com.cambistaonline.auth.domain.valueobjects.Password;
import com.cambistaonline.auth.domain.valueobjects.Ruc;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
@Primary
public class MongoUserPersistenceAdapter implements UserRepositoryPort, UserPersistencePort {

    private final SpringDataMongoUserRepository repository;

    public MongoUserPersistenceAdapter(SpringDataMongoUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User save(User user) {
        UserDocument doc = toDocument(user);
        UserDocument saved = repository.save(doc);
        return toDomain(saved);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return repository.findById(id.toString()).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return repository.findByEmail(email.getValue()).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return repository.existsByEmail(email.getValue());
    }

    private UserDocument toDocument(User domain) {
        LocalDateTime createdAt = domain.getCreatedAt() != null ? domain.getCreatedAt() : LocalDateTime.now();
        LocalDateTime updatedAt = domain.getUpdatedAt() != null ? domain.getUpdatedAt() : LocalDateTime.now();
        String idStr = domain.getId() != null ? domain.getId().toString() : UUID.randomUUID().toString();

        Optional<UserDocument> existing = repository.findByEmail(domain.getEmail().getValue());
        String mongoId = existing.map(UserDocument::getMongoId).orElse(null);

        return new UserDocument(
                mongoId,
                idStr,
                domain.getEmail() != null ? domain.getEmail().getValue() : null,
                domain.getPassword() != null ? domain.getPassword().getValue() : null,
                domain.getFirstName(),
                domain.getLastName(),
                domain.getDni() != null ? domain.getDni().getValue() : null,
                domain.getCompanyName(),
                domain.getRuc() != null ? domain.getRuc().getValue() : null,
                domain.getLegalRepresentativeName(),
                domain.getRole() != null ? domain.getRole() : "N",
                domain.getStatus() != null ? domain.getStatus().name() : UserStatus.ACTIVE.name(),
                createdAt,
                updatedAt
        );
    }

    private User toDomain(UserDocument doc) {
        UUID id = doc.getId() != null ? parseUUID(doc.getId()) : UUID.randomUUID();

        return User.builder()
                .id(id)
                .email(new Email(doc.getEmail()))
                .password(Password.fromHash(doc.getPassword()))
                .firstName(doc.getFirstName())
                .lastName(doc.getLastName())
                .dni(doc.getDni() != null && !doc.getDni().isBlank() ? new Dni(doc.getDni()) : null)
                .companyName(doc.getCompanyName())
                .ruc(doc.getRuc() != null && !doc.getRuc().isBlank() ? new Ruc(doc.getRuc()) : null)
                .legalRepresentativeName(doc.getLegalRepresentativeName())
                .role(doc.getRole() != null ? doc.getRole() : "N")
                .status(doc.getStatus() != null ? UserStatus.valueOf(doc.getStatus()) : UserStatus.ACTIVE)
                .createdAt(parseDateTime(doc.getCreatedAt()))
                .updatedAt(parseDateTime(doc.getUpdatedAt()))
                .build();
    }

    private UUID parseUUID(String str) {
        try {
            return UUID.fromString(str);
        } catch (Exception e) {
            return UUID.randomUUID();
        }
    }

    private LocalDateTime parseDateTime(Object input) {
        if (input == null) return LocalDateTime.now();
        if (input instanceof LocalDateTime ldt) return ldt;
        if (input instanceof Date date) return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        String str = input.toString().trim();
        try {
            if (str.contains(" ")) {
                str = str.replace(" ", "T");
            }
            if (str.contains("+")) {
                return OffsetDateTime.parse(str).toLocalDateTime();
            }
            return LocalDateTime.parse(str);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
