package io.openleap.iam.principal.domain.dto;

import java.util.UUID;

/**
 * Result of suspending a principal.
 */
public record PrincipalSuspended(
    /**
     * Principal ID that was suspended
     */
    UUID principalId
) {
}
