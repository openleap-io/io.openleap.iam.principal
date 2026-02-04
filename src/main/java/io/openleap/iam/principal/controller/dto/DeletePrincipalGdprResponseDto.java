package io.openleap.iam.principal.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for GDPR principal deletion.
 */
public class DeletePrincipalGdprResponseDto {

    /**
     * Principal ID
     */
    @JsonProperty("id")
    private String id;

    /**
     * Principal status (should be DELETED)
     */
    @JsonProperty("status")
    private String status;

    /**
     * Whether the principal data was anonymized
     */
    @JsonProperty("anonymized")
    private boolean anonymized;

    /**
     * Audit reference for tracking
     */
    @JsonProperty("audit_reference")
    private String auditReference;

    /**
     * Timestamp when the principal was deleted
     */
    @JsonProperty("deleted_at")
    private String deletedAt;

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

    public boolean isAnonymized() {
        return anonymized;
    }

    public void setAnonymized(boolean anonymized) {
        this.anonymized = anonymized;
    }

    public String getAuditReference() {
        return auditReference;
    }

    public void setAuditReference(String auditReference) {
        this.auditReference = auditReference;
    }

    public String getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(String deletedAt) {
        this.deletedAt = deletedAt;
    }
}
