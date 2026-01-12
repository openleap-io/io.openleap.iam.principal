package io.openleap.iam.principal.domain.dto;

import java.util.UUID;

/**
 * Result of deactivating a principal.
 */
public record PrincipalDeactivated(
    /**
     * Principal ID that was deactivated
     */
    UUID principalId
) {
}
