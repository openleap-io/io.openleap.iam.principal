package io.openleap.iam.principal.domain.dto;

import java.util.UUID;

/**
 * Command for rotating service principal credentials.
 */
public record RotateCredentialsCommand(
    /**
     * Principal ID
     */
    UUID id,

    /**
     * Force rotation even if not due
     */
    Boolean force,

    /**
     * Reason for rotation
     */
    String reason
) {
}
