package io.openleap.iam.principal.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for getting credential status of a service or system principal.
 */
public class GetCredentialStatusResponseDto {

    /**
     * Principal ID
     */
    @JsonProperty("principal_id")
    private String principalId;

    /**
     * Principal type (SERVICE or SYSTEM)
     */
    @JsonProperty("principal_type")
    private String principalType;

    /**
     * Next rotation due date (for SERVICE principals)
     */
    @JsonProperty("credential_rotation_date")
    private String credentialRotationDate;

    /**
     * Days until rotation is due
     */
    @JsonProperty("days_until_rotation")
    private Long daysUntilRotation;

    /**
     * Last rotation timestamp
     */
    @JsonProperty("last_rotated_at")
    private String lastRotatedAt;

    /**
     * Whether rotation is required (past due date)
     */
    @JsonProperty("rotation_required")
    private Boolean rotationRequired;

    /**
     * Whether the principal has an API key (SERVICE principals)
     */
    @JsonProperty("has_api_key")
    private Boolean hasApiKey;

    /**
     * Whether the principal has a certificate (SYSTEM principals)
     */
    @JsonProperty("has_certificate")
    private Boolean hasCertificate;

    // Getters and Setters

    public String getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

    public String getPrincipalType() {
        return principalType;
    }

    public void setPrincipalType(String principalType) {
        this.principalType = principalType;
    }

    public String getCredentialRotationDate() {
        return credentialRotationDate;
    }

    public void setCredentialRotationDate(String credentialRotationDate) {
        this.credentialRotationDate = credentialRotationDate;
    }

    public Long getDaysUntilRotation() {
        return daysUntilRotation;
    }

    public void setDaysUntilRotation(Long daysUntilRotation) {
        this.daysUntilRotation = daysUntilRotation;
    }

    public String getLastRotatedAt() {
        return lastRotatedAt;
    }

    public void setLastRotatedAt(String lastRotatedAt) {
        this.lastRotatedAt = lastRotatedAt;
    }

    public Boolean getRotationRequired() {
        return rotationRequired;
    }

    public void setRotationRequired(Boolean rotationRequired) {
        this.rotationRequired = rotationRequired;
    }

    public Boolean getHasApiKey() {
        return hasApiKey;
    }

    public void setHasApiKey(Boolean hasApiKey) {
        this.hasApiKey = hasApiKey;
    }

    public Boolean getHasCertificate() {
        return hasCertificate;
    }

    public void setHasCertificate(Boolean hasCertificate) {
        this.hasCertificate = hasCertificate;
    }
}
