package io.openleap.iam.principal.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for activating a principal.
 */
public class ActivatePrincipalRequestDto {
    
    /**
     * Email verification token (for self-activation)
     */
    @JsonProperty("verification_token")
    private String verificationToken;
    
    /**
     * Admin override flag (for admin activation)
     */
    @JsonProperty("admin_override")
    private Boolean adminOverride;
    
    /**
     * Reason for admin activation (optional)
     */
    @JsonProperty("reason")
    private String reason;

    // Getters and Setters
    
    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public Boolean getAdminOverride() {
        return adminOverride;
    }

    public void setAdminOverride(Boolean adminOverride) {
        this.adminOverride = adminOverride;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
