package io.openleap.iam.principal.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for deactivating a principal.
 */
public class DeactivatePrincipalResponseDto {

    /**
     * Principal ID that was deactivated
     */
    @JsonProperty("id")
    private String id;

    /**
     * Status after deactivation (should be INACTIVE)
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
