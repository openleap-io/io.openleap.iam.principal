package io.openleap.iam.principal.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for getting a principal by ID.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetPrincipalResponseDto {

    @JsonProperty("principal_id")
    private String principalId;

    @JsonProperty("principal_type")
    private String principalType;

    @JsonProperty("username")
    private String username;

    @JsonProperty("email")
    private String email;

    @JsonProperty("status")
    private String status;

    @JsonProperty("primary_tenant_id")
    private String primaryTenantId;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("context_tags")
    private Map<String, Object> contextTags;

    // Human principal specific fields
    @JsonProperty("email_verified")
    private Boolean emailVerified;

    @JsonProperty("mfa_enabled")
    private Boolean mfaEnabled;

    @JsonProperty("last_login_at")
    private String lastLoginAt;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    // Service principal specific fields
    @JsonProperty("service_name")
    private String serviceName;

    @JsonProperty("allowed_scopes")
    private List<String> allowedScopes;

    @JsonProperty("credential_rotation_date")
    private String credentialRotationDate;

    // System principal specific fields
    @JsonProperty("system_identifier")
    private String systemIdentifier;

    @JsonProperty("integration_type")
    private String integrationType;

    @JsonProperty("allowed_operations")
    private List<String> allowedOperations;

    // Device principal specific fields
    @JsonProperty("device_identifier")
    private String deviceIdentifier;

    @JsonProperty("device_type")
    private String deviceType;

    @JsonProperty("manufacturer")
    private String manufacturer;

    @JsonProperty("model")
    private String model;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPrimaryTenantId() {
        return primaryTenantId;
    }

    public void setPrimaryTenantId(String primaryTenantId) {
        this.primaryTenantId = primaryTenantId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Map<String, Object> getContextTags() {
        return contextTags;
    }

    public void setContextTags(Map<String, Object> contextTags) {
        this.contextTags = contextTags;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Boolean getMfaEnabled() {
        return mfaEnabled;
    }

    public void setMfaEnabled(Boolean mfaEnabled) {
        this.mfaEnabled = mfaEnabled;
    }

    public String getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(String lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<String> getAllowedScopes() {
        return allowedScopes;
    }

    public void setAllowedScopes(List<String> allowedScopes) {
        this.allowedScopes = allowedScopes;
    }

    public String getCredentialRotationDate() {
        return credentialRotationDate;
    }

    public void setCredentialRotationDate(String credentialRotationDate) {
        this.credentialRotationDate = credentialRotationDate;
    }

    public String getSystemIdentifier() {
        return systemIdentifier;
    }

    public void setSystemIdentifier(String systemIdentifier) {
        this.systemIdentifier = systemIdentifier;
    }

    public String getIntegrationType() {
        return integrationType;
    }

    public void setIntegrationType(String integrationType) {
        this.integrationType = integrationType;
    }

    public List<String> getAllowedOperations() {
        return allowedOperations;
    }

    public void setAllowedOperations(List<String> allowedOperations) {
        this.allowedOperations = allowedOperations;
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
