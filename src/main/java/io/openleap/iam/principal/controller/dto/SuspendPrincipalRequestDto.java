package io.openleap.iam.principal.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for suspending a principal.
 */
public class SuspendPrincipalRequestDto {
    
    /**
     * Reason for suspension (required)
     */
    @NotBlank(message = "Reason is required")
    @JsonProperty("reason")
    private String reason;
    
    /**
     * Incident ticket reference (optional)
     */
    @JsonProperty("incident_ticket")
    private String incidentTicket;

    // Getters and Setters
    
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getIncidentTicket() {
        return incidentTicket;
    }

    public void setIncidentTicket(String incidentTicket) {
        this.incidentTicket = incidentTicket;
    }
}
