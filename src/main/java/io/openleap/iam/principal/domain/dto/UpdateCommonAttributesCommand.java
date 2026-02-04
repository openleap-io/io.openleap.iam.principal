package io.openleap.iam.principal.domain.dto;

import java.util.Map;
import java.util.UUID;

/**
 * Command for updating common attributes on a principal.
 */
public record UpdateCommonAttributesCommand(
    /**
     * Principal ID to update
     */
    UUID id,

    /**
     * Optional business hints (lightweight classification only, max 10KB)
     */
    Map<String, Object> contextTags
) {
}
