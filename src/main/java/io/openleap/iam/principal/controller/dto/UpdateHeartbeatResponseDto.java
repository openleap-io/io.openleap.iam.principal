package io.openleap.iam.principal.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for updating a device principal's heartbeat.
 */
public class UpdateHeartbeatResponseDto {

    /**
     * Device principal ID
     */
    @JsonProperty("id")
    private String id;

    /**
     * Timestamp of the heartbeat
     */
    @JsonProperty("last_heartbeat_at")
    private String lastHeartbeatAt;

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLastHeartbeatAt() {
        return lastHeartbeatAt;
    }

    public void setLastHeartbeatAt(String lastHeartbeatAt) {
        this.lastHeartbeatAt = lastHeartbeatAt;
    }
}
