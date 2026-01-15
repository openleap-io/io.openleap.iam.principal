package io.openleap.iam.principal.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for updating a device principal's heartbeat.
 */
public class UpdateHeartbeatResponseDto {

    /**
     * Device principal ID
     */
    @JsonProperty("principal_id")
    private String principalId;

    /**
     * Timestamp of the heartbeat
     */
    @JsonProperty("last_heartbeat_at")
    private String lastHeartbeatAt;

    // Getters and Setters

    public String getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

    public String getLastHeartbeatAt() {
        return lastHeartbeatAt;
    }

    public void setLastHeartbeatAt(String lastHeartbeatAt) {
        this.lastHeartbeatAt = lastHeartbeatAt;
    }
}
