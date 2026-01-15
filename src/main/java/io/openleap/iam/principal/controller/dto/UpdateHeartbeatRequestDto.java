package io.openleap.iam.principal.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Request DTO for updating a device principal's heartbeat.
 */
public class UpdateHeartbeatRequestDto {

    /**
     * Optional firmware version update
     */
    @JsonProperty("firmware_version")
    private String firmwareVersion;

    /**
     * Optional location info update
     */
    @JsonProperty("location_info")
    private Map<String, Object> locationInfo;

    // Getters and Setters

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public Map<String, Object> getLocationInfo() {
        return locationInfo;
    }

    public void setLocationInfo(Map<String, Object> locationInfo) {
        this.locationInfo = locationInfo;
    }
}
