package io.openleap.iam.principal.domain.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "service_principals", schema = "iam_principal")
public class ServicePrincipalEntity extends Principal {
    
    /**
     * Keycloak client ID (UK, nullable, set after sync)
     */
    @Column(name = "keycloak_client_id", unique = true, length = 255)
    private String keycloakClientId;
    
    /**
     * Service identifier (UK, required, max 200 chars)
     */
    @Column(name = "service_name", nullable = false, unique = true, length = 200)
    private String serviceName;
    
    /**
     * OAuth2 scopes (array of scope strings)
     */
    @Column(name = "allowed_scopes", columnDefinition = "text[]")
    private List<String> allowedScopes;
    
    /**
     * Hashed API key (SHA-256, never store plain text)
     */
    @Column(name = "api_key_hash", nullable = false, length = 64)
    private String apiKeyHash;
    
    /**
     * Next rotation due date (required, default +90 days)
     */
    @Column(name = "credential_rotation_date", nullable = false)
    private LocalDate credentialRotationDate;
    
    /**
     * Last rotation timestamp
     */
    @Column(name = "rotated_at")
    private Instant rotatedAt;
    
    @Override
    public PrincipalType getPrincipalType() {
        return PrincipalType.SERVICE;
    }
    
    // Getters and Setters
    
    public String getKeycloakClientId() {
        return keycloakClientId;
    }
    
    public void setKeycloakClientId(String keycloakClientId) {
        this.keycloakClientId = keycloakClientId;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public List<String> getAllowedScopes() {
        return allowedScopes;
    }
    
    public void setAllowedScopes(List<String> allowedScopes) {
        this.allowedScopes = allowedScopes;
    }
    
    public String getApiKeyHash() {
        return apiKeyHash;
    }
    
    public void setApiKeyHash(String apiKeyHash) {
        this.apiKeyHash = apiKeyHash;
    }
    
    public LocalDate getCredentialRotationDate() {
        return credentialRotationDate;
    }
    
    public void setCredentialRotationDate(LocalDate credentialRotationDate) {
        this.credentialRotationDate = credentialRotationDate;
    }
    
    public Instant getRotatedAt() {
        return rotatedAt;
    }
    
    public void setRotatedAt(Instant rotatedAt) {
        this.rotatedAt = rotatedAt;
    }
}

