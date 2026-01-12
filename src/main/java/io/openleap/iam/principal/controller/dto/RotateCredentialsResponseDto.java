package io.openleap.iam.principal.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for rotating service principal credentials.
 */
public class RotateCredentialsResponseDto {

    /**
     * Principal ID
     */
    @JsonProperty("principal_id")
    private String principalId;

    /**
     * New API key (returned only once)
     */
    @JsonProperty("api_key")
    private String apiKey;

    /**
     * New Keycloak client secret (returned only once)
     */
    @JsonProperty("keycloak_client_secret")
    private String keycloakClientSecret;

    /**
     * Next rotation date
     */
    @JsonProperty("credential_rotation_date")
    private String credentialRotationDate;

    /**
     * Timestamp when credentials were rotated
     */
    @JsonProperty("rotated_at")
    private String rotatedAt;

    /**
     * Warning message about storing credentials securely
     */
    @JsonProperty("warning")
    private String warning;

    // Getters and Setters

    public String getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getKeycloakClientSecret() {
        return keycloakClientSecret;
    }

    public void setKeycloakClientSecret(String keycloakClientSecret) {
        this.keycloakClientSecret = keycloakClientSecret;
    }

    public String getCredentialRotationDate() {
        return credentialRotationDate;
    }

    public void setCredentialRotationDate(String credentialRotationDate) {
        this.credentialRotationDate = credentialRotationDate;
    }

    public String getRotatedAt() {
        return rotatedAt;
    }

    public void setRotatedAt(String rotatedAt) {
        this.rotatedAt = rotatedAt;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }
}
