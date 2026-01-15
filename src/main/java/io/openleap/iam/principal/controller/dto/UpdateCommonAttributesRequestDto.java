package io.openleap.iam.principal.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Request DTO for updating common attributes on a principal.
 */
public class UpdateCommonAttributesRequestDto {

    /**
     * Optional business hints (lightweight classification only, max 10KB)
     */
    @JsonProperty("context_tags")
    private Map<String, Object> contextTags;

    // Getters and Setters

    public Map<String, Object> getContextTags() {
        return contextTags;
    }

    public void setContextTags(Map<String, Object> contextTags) {
        this.contextTags = contextTags;
    }
}
