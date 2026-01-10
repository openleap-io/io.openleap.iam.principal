package io.openleap.iam.principal.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CreateServicePrincipalRequestDto {
    
    @NotBlank(message = "Service name is required")
    @Size(max = 200, message = "Service name must not exceed 200 characters")
    private String serviceName;
    
    @NotNull(message = "Primary tenant ID is required")
    private UUID primaryTenantId;
    
    private Map<String, Object> contextTags;
    
    private List<String> allowedScopes;
    
    // Getters and Setters
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public UUID getPrimaryTenantId() {
        return primaryTenantId;
    }
    
    public void setPrimaryTenantId(UUID primaryTenantId) {
        this.primaryTenantId = primaryTenantId;
    }
    
    public Map<String, Object> getContextTags() {
        return contextTags;
    }
    
    public void setContextTags(Map<String, Object> contextTags) {
        this.contextTags = contextTags;
    }
    
    public List<String> getAllowedScopes() {
        return allowedScopes;
    }
    
    public void setAllowedScopes(List<String> allowedScopes) {
        this.allowedScopes = allowedScopes;
    }
}
