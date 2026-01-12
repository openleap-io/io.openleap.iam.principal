package io.openleap.iam.principal.domain.event;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Event payload for iam.principal.credentials.rotated event.
 *
 * Published when service principal credentials are rotated.
 *
 * See spec/iam_principal_spec.md Section 5.1 - iam.principal.credentials.rotated
 */
public record CredentialsRotatedEvent(
    /**
     * Principal unique identifier
     */
    UUID principalId,

    /**
     * Service name
     */
    String serviceName,

    /**
     * Next rotation date
     */
    LocalDate credentialRotationDate,

    /**
     * Timestamp when credentials were rotated
     */
    Instant rotatedAt,

    /**
     * Reason for rotation (if provided)
     */
    String reason
) {
}
