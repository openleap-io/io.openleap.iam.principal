package io.openleap.iam.principal.controller.dto;

import java.util.List;
import java.util.UUID;

public class CreateSystemPrincipalResponseDto {
    
    private UUID principalId;
    private String principalType;
    private String username;
    private String status;
    private SystemPrincipalInfo systemPrincipal;
    private String createdAt;
    
    public CreateSystemPrincipalResponseDto() {
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
    
    public SystemPrincipalInfo getSystemPrincipal() {
        return systemPrincipal;
    }
    
    public void setSystemPrincipal(SystemPrincipalInfo systemPrincipal) {
        this.systemPrincipal = systemPrincipal;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public static class SystemPrincipalInfo {
        private String systemIdentifier;
        private String integrationType;
        private String certificateThumbprint;
        private List<String> allowedOperations;
        
        public SystemPrincipalInfo() {
        }
        
        public String getSystemIdentifier() {
            return systemIdentifier;
        }
        
        public void setSystemIdentifier(String systemIdentifier) {
            this.systemIdentifier = systemIdentifier;
        }
        
        public String getIntegrationType() {
            return integrationType;
        }
        
        public void setIntegrationType(String integrationType) {
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
}
