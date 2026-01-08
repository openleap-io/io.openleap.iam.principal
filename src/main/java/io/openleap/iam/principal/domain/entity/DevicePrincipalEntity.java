package io.openleap.iam.principal.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "device_principals", schema = "iam_principal")
public class DevicePrincipalEntity extends Principal {
    
    /**
     * Keycloak client ID (UK, nullable, set after sync)
     */
    @Column(name = "keycloak_client_id", unique = true, length = 255)
    private String keycloakClientId;
    
    /**
     * Unique device ID (serial, MAC, etc.) (UK, required, max 200 chars)
     */
    @Column(name = "device_identifier", nullable = false, unique = true, length = 200)
    private String deviceIdentifier;
    
    /**
     * Device category
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 50)
    private DeviceType deviceType;
    
    /**
     * Device manufacturer (optional, max 100 chars)
     */
    @Column(name = "manufacturer", length = 100)
    private String manufacturer;
    
    /**
     * Device model (optional, max 100 chars)
     */
    @Column(name = "model", length = 100)
    private String model;
    
    /**
     * Current firmware version (optional, max 50 chars)
     */
    @Column(name = "firmware_version", length = 50)
    private String firmwareVersion;
    
    /**
     * Device certificate fingerprint (SHA-256 thumbprint)
     */
    @Column(name = "certificate_thumbprint", length = 64)
    private String certificateThumbprint;
    
    /**
     * Last device check-in timestamp
     */
    @Column(name = "last_heartbeat_at")
    private Instant lastHeartbeatAt;
    
    /**
     * Physical location metadata (optional, max 5KB)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "location_info", columnDefinition = "jsonb")
    private Map<String, Object> locationInfo;
    
    @Override
    public PrincipalType getPrincipalType() {
        return PrincipalType.DEVICE;
    }
    
    // Getters and Setters
    
    public String getKeycloakClientId() {
        return keycloakClientId;
    }
    
    public void setKeycloakClientId(String keycloakClientId) {
        this.keycloakClientId = keycloakClientId;
    }
    
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
    
    public Instant getLastHeartbeatAt() {
        return lastHeartbeatAt;
    }
    
    public void setLastHeartbeatAt(Instant lastHeartbeatAt) {
        this.lastHeartbeatAt = lastHeartbeatAt;
    }
    
    public Map<String, Object> getLocationInfo() {
        return locationInfo;
    }
    
    public void setLocationInfo(Map<String, Object> locationInfo) {
        this.locationInfo = locationInfo;
    }
}

