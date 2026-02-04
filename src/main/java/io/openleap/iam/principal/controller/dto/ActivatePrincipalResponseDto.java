package io.openleap.iam.principal.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for activating a principal.
 */
public class ActivatePrincipalResponseDto {
    
    /**
     * Principal ID that was activated
     */
    @JsonProperty("id")
    private String id;
    
    /**
     * Status after activation (should be ACTIVE)
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
