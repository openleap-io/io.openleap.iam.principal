package io.openleap.iam.principal.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for adding a tenant membership.
 */
public class AddTenantMembershipResponseDto {

    /**
     * Membership ID
     */
    @JsonProperty("id")
    private String id;

    /**
     * Principal ID
     */
    @JsonProperty("principal_id")
    private String principalId;

    /**
     * Tenant ID
     */
    @JsonProperty("tenant_id")
    private String tenantId;

    /**
     * Membership start date
     */
    @JsonProperty("valid_from")
    private String validFrom;

    /**
     * Membership end date
     */
    @JsonProperty("valid_to")
    private String validTo;

    /**
     * Membership status
     */
    @JsonProperty("status")
    private String status;

    /**
     * Principal ID of the user who added the membership
     */
    @JsonProperty("invited_by")
    private String invitedBy;

    /**
     * Timestamp when the membership was created
     */
    @JsonProperty("created_at")
    private String createdAt;

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(String validFrom) {
        this.validFrom = validFrom;
    }

    public String getValidTo() {
        return validTo;
    }

    public void setValidTo(String validTo) {
        this.validTo = validTo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(String invitedBy) {
        this.invitedBy = invitedBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
