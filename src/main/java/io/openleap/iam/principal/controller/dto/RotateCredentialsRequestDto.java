package io.openleap.iam.principal.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for rotating service principal credentials.
 */
public class RotateCredentialsRequestDto {

    /**
     * Force rotation even if not due (optional, defaults to false)
     */
    @JsonProperty("force")
    private Boolean force;

    /**
     * Reason for rotation (optional)
     */
    @JsonProperty("reason")
    private String reason;

    // Getters and Setters

    public Boolean getForce() {
        return force;
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
