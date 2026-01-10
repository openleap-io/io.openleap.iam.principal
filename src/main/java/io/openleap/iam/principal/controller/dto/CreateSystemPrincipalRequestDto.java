package io.openleap.iam.principal.controller.dto;

import io.openleap.iam.principal.domain.entity.IntegrationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CreateSystemPrincipalRequestDto {
    
    @NotBlank(message = "System identifier is required")
    @Size(max = 200, message = "System identifier must not exceed 200 characters")
    private String systemIdentifier;
    
    private IntegrationType integrationType;
    
    @NotNull(message = "Primary tenant ID is required")
    private UUID primaryTenantId;
    
    @NotBlank(message = "Certificate thumbprint is required")
    @Size(max = 64, message = "Certificate thumbprint must not exceed 64 characters")
    private String certificateThumbprint;
    
    private Map<String, Object> contextTags;
    
    private List<String> allowedOperations;
    
    // Getters and Setters
    
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
    
    public UUID getPrimaryTenantId() {
        return primaryTenantId;
    }
    
    public void setPrimaryTenantId(UUID primaryTenantId) {
        this.primaryTenantId = primaryTenantId;
    }
    
    public String getCertificateThumbprint() {
        return certificateThumbprint;
    }
    
    public void setCertificateThumbprint(String certificateThumbprint) {
        this.certificateThumbprint = certificateThumbprint;
    }
    
    public Map<String, Object> getContextTags() {
        return contextTags;
    }
    
    public void setContextTags(Map<String, Object> contextTags) {
        this.contextTags = contextTags;
    }
    
    public List<String> getAllowedOperations() {
        return allowedOperations;
    }
    
    public void setAllowedOperations(List<String> allowedOperations) {
        this.allowedOperations = allowedOperations;
    }
}
