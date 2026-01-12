package io.openleap.iam.principal.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for GDPR principal deletion.
 */
public class DeletePrincipalGdprRequestDto {

    /**
     * Confirmation string - must be "DELETE" (required)
     */
    @NotBlank(message = "Confirmation is required")
    @Pattern(regexp = "DELETE", message = "Confirmation must be 'DELETE'")
    @JsonProperty("confirmation")
    private String confirmation;

    /**
     * GDPR request ticket reference (required)
     */
    @NotBlank(message = "GDPR request ticket is required")
    @JsonProperty("gdpr_request_ticket")
    private String gdprRequestTicket;

    /**
     * Email of the requestor (required)
     */
    @NotBlank(message = "Requestor email is required")
    @Email(message = "Requestor email must be valid")
    @JsonProperty("requestor_email")
    private String requestorEmail;

    // Getters and Setters

    public String getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(String confirmation) {
        this.confirmation = confirmation;
    }

    public String getGdprRequestTicket() {
        return gdprRequestTicket;
    }

    public void setGdprRequestTicket(String gdprRequestTicket) {
        this.gdprRequestTicket = gdprRequestTicket;
    }

    public String getRequestorEmail() {
        return requestorEmail;
    }

    public void setRequestorEmail(String requestorEmail) {
        this.requestorEmail = requestorEmail;
    }
}
