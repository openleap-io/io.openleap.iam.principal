package io.openleap.iam.principal.domain.dto;

import java.util.UUID;

/**
 * Result of updating common attributes on a principal.
 */
public record CommonAttributesUpdated(
    /**
     * Principal ID that was updated
     */
    UUID id
) {
}
