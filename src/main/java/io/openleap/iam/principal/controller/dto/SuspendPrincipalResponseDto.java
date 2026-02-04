package io.openleap.iam.principal.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for suspending a principal.
 */
public class SuspendPrincipalResponseDto {
    
    /**
     * Principal ID that was suspended
     */
    @JsonProperty("id")
    private String id;
    
    /**
     * Status after suspension (should be SUSPENDED)
     */
    @JsonProperty("status")
    private String status;

    // Getters and Setters
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
