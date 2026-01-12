package io.openleap.iam.principal.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/**
 * Request DTO for deactivating a principal.
 */
public class DeactivatePrincipalRequestDto {

    /**
     * Reason for deactivation (required)
     */
    @NotBlank(message = "Reason is required")
    @JsonProperty("reason")
    private String reason;

    /**
     * Effective date for deactivation (optional, defaults to now)
     */
    @JsonProperty("effective_date")
    private LocalDate effectiveDate;

    // Getters and Setters

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }
}
