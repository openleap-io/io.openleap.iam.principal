package io.openleap.iam.principal.controller.dto;

import java.util.UUID;

public class CreateDevicePrincipalResponseDto {
    
    private UUID principalId;
    private String principalType;
    private String username;
    private String status;
    private DevicePrincipalInfo devicePrincipal;
    private String createdAt;
    
    public CreateDevicePrincipalResponseDto() {
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
    
    public DevicePrincipalInfo getDevicePrincipal() {
        return devicePrincipal;
    }
    
    public void setDevicePrincipal(DevicePrincipalInfo devicePrincipal) {
        this.devicePrincipal = devicePrincipal;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public static class DevicePrincipalInfo {
        private String deviceIdentifier;
        private String deviceType;
        private String manufacturer;
        private String model;
        
        public DevicePrincipalInfo() {
        }
        
        public String getDeviceIdentifier() {
            return deviceIdentifier;
        }
        
        public void setDeviceIdentifier(String deviceIdentifier) {
            this.deviceIdentifier = deviceIdentifier;
        }
        
        public String getDeviceType() {
            return deviceType;
        }
        
        public void setDeviceType(String deviceType) {
            this.deviceType = deviceType;
        }
        
        public String getManufacturer() {
            return manufacturer;
        }
        
        public void setManufacturer(String manufacturer) {
            this.manufacturer = manufacturer;
        }
        
        public String getModel() {
            return model;
        }
        
        public void setModel(String model) {
            this.model = model;
        }
    }
}
