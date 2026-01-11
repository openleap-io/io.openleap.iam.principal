package io.openleap.iam.principal.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for suspending a principal.
 */
public class SuspendPrincipalResponseDto {
    
    /**
     * Principal ID that was suspended
     */
    @JsonProperty("principal_id")
    private String principalId;
    
    /**
     * Status after suspension (should be SUSPENDED)
     */
    @JsonProperty("status")
    private String status;

    // Getters and Setters
    
    public String getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
