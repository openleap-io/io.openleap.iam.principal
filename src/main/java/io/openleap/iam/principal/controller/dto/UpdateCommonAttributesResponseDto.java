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
    @JsonProperty("id")
    private String id;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
