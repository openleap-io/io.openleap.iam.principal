package io.openleap.iam.principal.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for adding a tenant membership.
 */
public class AddTenantMembershipRequestDto {

    /**
     * Tenant ID to add membership for
     */
    @NotNull
    @JsonProperty("tenant_id")
    private String tenantId;

    /**
     * Membership start date (optional, defaults to today)
     */
    @JsonProperty("valid_from")
    private String validFrom;

    /**
     * Membership end date (optional, null = no expiry)
     */
    @JsonProperty("valid_to")
    private String validTo;

    // Getters and Setters

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
}
