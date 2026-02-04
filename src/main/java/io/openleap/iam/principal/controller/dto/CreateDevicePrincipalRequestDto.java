package io.openleap.iam.principal.controller.dto;

import io.openleap.iam.principal.domain.entity.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.UUID;

public class CreateDevicePrincipalRequestDto {
    
    @NotBlank(message = "Device identifier is required")
    @Size(max = 200, message = "Device identifier must not exceed 200 characters")
    private String deviceIdentifier;
    
    @NotNull(message = "Device type is required")
    private DeviceType deviceType;

    private UUID defaultTenantId;

    @Size(max = 100, message = "Manufacturer must not exceed 100 characters")
    private String manufacturer;
    
    @Size(max = 100, message = "Model must not exceed 100 characters")
    private String model;
    
    @Size(max = 50, message = "Firmware version must not exceed 50 characters")
    private String firmwareVersion;
    
    @NotBlank(message = "Certificate thumbprint is required")
    @Size(max = 64, message = "Certificate thumbprint must not exceed 64 characters")
    private String certificateThumbprint;
    
    private Map<String, Object> locationInfo;
    
    private Map<String, Object> contextTags;
    
    // Getters and Setters
    
    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }
    
    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }
    
    public DeviceType getDeviceType() {
        return deviceType;
    }
    
    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }
    
    public UUID getDefaultTenantId() {
        return defaultTenantId;
    }
    
    public void setDefaultTenantId(UUID defaultTenantId) {
        this.defaultTenantId = defaultTenantId;
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
    
    public String getFirmwareVersion() {
        return firmwareVersion;
    }
    
    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }
    
    public String getCertificateThumbprint() {
        return certificateThumbprint;
    }
    
    public void setCertificateThumbprint(String certificateThumbprint) {
        this.certificateThumbprint = certificateThumbprint;
    }
    
    public Map<String, Object> getLocationInfo() {
        return locationInfo;
    }
    
    public void setLocationInfo(Map<String, Object> locationInfo) {
        this.locationInfo = locationInfo;
    }
    
    public Map<String, Object> getContextTags() {
        return contextTags;
    }
    
    public void setContextTags(Map<String, Object> contextTags) {
        this.contextTags = contextTags;
    }
}
