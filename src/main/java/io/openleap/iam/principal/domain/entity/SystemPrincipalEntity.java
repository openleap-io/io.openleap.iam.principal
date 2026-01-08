package io.openleap.iam.principal.domain.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "system_principals", schema = "iam_principal")
public class SystemPrincipalEntity extends Principal {
    
    /**
     * Keycloak client ID (UK, nullable, set after sync)
     */
    @Column(name = "keycloak_client_id", unique = true, length = 255)
    private String keycloakClientId;
    
    /**
     * External system ID (UK, required, max 200 chars)
     */
    @Column(name = "system_identifier", nullable = false, unique = true, length = 200)
    private String systemIdentifier;
    
    /**
     * Integration category
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "integration_type", length = 100)
    private IntegrationType integrationType;
    
    /**
     * mTLS certificate fingerprint (SHA-256 thumbprint)
     */
    @Column(name = "certificate_thumbprint", length = 64)
    private String certificateThumbprint;
    
    /**
     * Permitted operations (whitelist of operations)
     */
    @Column(name = "allowed_operations", columnDefinition = "text[]")
    private List<String> allowedOperations;
    
    @Override
    public PrincipalType getPrincipalType() {
        return PrincipalType.SYSTEM;
    }
    
    // Getters and Setters
    
    public String getKeycloakClientId() {
        return keycloakClientId;
    }
    
    public void setKeycloakClientId(String keycloakClientId) {
        this.keycloakClientId = keycloakClientId;
    }
    
    public String getSystemIdentifier() {
        return systemIdentifier;
    }
    
    public void setSystemIdentifier(String systemIdentifier) {
        this.systemIdentifier = systemIdentifier;
    }
    
    public IntegrationType getIntegrationType() {
        return integrationType;
    }
    
    public void setIntegrationType(IntegrationType integrationType) {
        this.integrationType = integrationType;
    }
    
    public String getCertificateThumbprint() {
        return certificateThumbprint;
    }
    
    public void setCertificateThumbprint(String certificateThumbprint) {
        this.certificateThumbprint = certificateThumbprint;
    }
    
    public List<String> getAllowedOperations() {
        return allowedOperations;
    }
    
    public void setAllowedOperations(List<String> allowedOperations) {
        this.allowedOperations = allowedOperations;
    }
}

