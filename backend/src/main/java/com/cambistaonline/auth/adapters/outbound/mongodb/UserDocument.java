package com.cambistaonline.auth.adapters.outbound.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "users")
public class UserDocument {

    @Id
    private String mongoId;

    @Field("id")
    private String id;

    @Field("email")
    private String email;

    @Field("password")
    private String password;

    @Field("first_name")
    private String firstName;

    @Field("last_name")
    private String lastName;

    @Field("dni")
    private String dni;

    @Field("company_name")
    private String companyName;

    @Field("ruc")
    private String ruc;

    @Field("legal_representative_name")
    private String legalRepresentativeName;

    @Field("role")
    private String role;

    @Field("status")
    private String status;

    @Field("created_at")
    private Object createdAt;

    @Field("updated_at")
    private Object updatedAt;

    @Field("mfa_enabled")
    private Boolean mfaEnabled;

    @Field("mfa_secret")
    private String mfaSecret;

    @Field("auth_provider")
    private String authProvider;

    @Field("provider_id")
    private String providerId;

    public UserDocument() {}

    public UserDocument(String mongoId, String id, String email, String password, String firstName, String lastName, String dni, String companyName, String ruc, String legalRepresentativeName, String role, String status, Object createdAt, Object updatedAt) {
        this(mongoId, id, email, password, firstName, lastName, dni, companyName, ruc, legalRepresentativeName, role, status, createdAt, updatedAt, false, null, "LOCAL", null);
    }

    public UserDocument(String mongoId, String id, String email, String password, String firstName, String lastName, String dni, String companyName, String ruc, String legalRepresentativeName, String role, String status, Object createdAt, Object updatedAt, Boolean mfaEnabled, String mfaSecret, String authProvider, String providerId) {
        this.mongoId = mongoId;
        this.id = id;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dni = dni;
        this.companyName = companyName;
        this.ruc = ruc;
        this.legalRepresentativeName = legalRepresentativeName;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.mfaEnabled = mfaEnabled;
        this.mfaSecret = mfaSecret;
        this.authProvider = authProvider;
        this.providerId = providerId;
    }

    public String getMongoId() { return mongoId; }
    public void setMongoId(String mongoId) { this.mongoId = mongoId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public String getLegalRepresentativeName() { return legalRepresentativeName; }
    public void setLegalRepresentativeName(String legalRepresentativeName) { this.legalRepresentativeName = legalRepresentativeName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Object getCreatedAt() { return createdAt; }
    public void setCreatedAt(Object createdAt) { this.createdAt = createdAt; }

    public Object getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Object updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getMfaEnabled() { return mfaEnabled; }
    public void setMfaEnabled(Boolean mfaEnabled) { this.mfaEnabled = mfaEnabled; }

    public String getMfaSecret() { return mfaSecret; }
    public void setMfaSecret(String mfaSecret) { this.mfaSecret = mfaSecret; }

    public String getAuthProvider() { return authProvider; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
}
