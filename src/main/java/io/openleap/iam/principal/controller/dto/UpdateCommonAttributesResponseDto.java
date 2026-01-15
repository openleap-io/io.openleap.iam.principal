package io.openleap.iam.principal.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Response DTO for updating common attributes on a principal.
 */
public class UpdateCommonAttributesResponseDto {

    /**
     * Principal ID that was updated
     */
    @JsonProperty("principal_id")
    private String principalId;

    /**
     * Updated context tags
     */
    @JsonProperty("context_tags")
    private Map<String, Object> contextTags;

    /**
     * Timestamp when the principal was updated
     */
    @JsonProperty("updated_at")
    private String updatedAt;

    // Getters and Setters

    public String getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

    public Map<String, Object> getContextTags() {
        return contextTags;
    }

    public void setContextTags(Map<String, Object> contextTags) {
        this.contextTags = contextTags;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
