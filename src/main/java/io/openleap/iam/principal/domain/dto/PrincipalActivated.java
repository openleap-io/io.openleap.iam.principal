package io.openleap.iam.principal.domain.dto;

import java.util.UUID;

/**
 * Result of activating a principal.
 */
public record PrincipalActivated(
    /**
     * Principal ID that was activated
     */
    UUID principalId
) {
}
