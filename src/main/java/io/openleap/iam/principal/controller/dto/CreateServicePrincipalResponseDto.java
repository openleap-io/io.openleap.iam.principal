package io.openleap.iam.principal.controller.dto;

import java.util.List;
import java.util.UUID;

public class CreateServicePrincipalResponseDto {
    
    private UUID principalId;
    private String principalType;
    private String username;
    private String status;
    private ServicePrincipalInfo servicePrincipal;
    private String keycloakClientId;
    private String keycloakClientSecret;
    private String createdAt;
    private String warning;
    
    public CreateServicePrincipalResponseDto() {
    }
    
    public UUID getPrincipalId() {
        return principalId;
    }
    
    public void setPrincipalId(UUID principalId) {
        this.principalId = principalId;
    }
    
    public String getPrincipalType() {
        return principalType;
    }
    
    public void setPrincipalType(String principalType) {
        this.principalType = principalType;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public ServicePrincipalInfo getServicePrincipal() {
        return servicePrincipal;
    }
    
    public void setServicePrincipal(ServicePrincipalInfo servicePrincipal) {
        this.servicePrincipal = servicePrincipal;
    }
    
    public String getKeycloakClientId() {
        return keycloakClientId;
    }
    
    public void setKeycloakClientId(String keycloakClientId) {
        this.keycloakClientId = keycloakClientId;
    }
    
    public String getKeycloakClientSecret() {
        return keycloakClientSecret;
    }
    
    public void setKeycloakClientSecret(String keycloakClientSecret) {
        this.keycloakClientSecret = keycloakClientSecret;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getWarning() {
        return warning;
    }
    
    public void setWarning(String warning) {
        this.warning = warning;
    }
    
    public static class ServicePrincipalInfo {
        private String serviceName;
        private String apiKey;
        private List<String> allowedScopes;
        private String credentialRotationDate;
        
        public ServicePrincipalInfo() {
        }
        
        public String getServiceName() {
            return serviceName;
        }
        
        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }
        
        public String getApiKey() {
            return apiKey;
        }
        
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        
        public List<String> getAllowedScopes() {
            return allowedScopes;
        }
        
        public void setAllowedScopes(List<String> allowedScopes) {
            this.allowedScopes = allowedScopes;
        }
        
        public String getCredentialRotationDate() {
            return credentialRotationDate;
        }
        
        public void setCredentialRotationDate(String credentialRotationDate) {
            this.credentialRotationDate = credentialRotationDate;
        }
    }
}
