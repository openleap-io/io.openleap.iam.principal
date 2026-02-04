package io.openleap.iam.principal.domain.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Result containing credential status for a service or system principal.
 */
public record CredentialStatus(
    /**
     * Principal ID
     */
    UUID id,

    /**
     * Principal type (SERVICE or SYSTEM)
     */
    String principalType,

    /**
     * Next rotation due date (for SERVICE principals)
     */
    LocalDate credentialRotationDate,

    /**
     * Days until rotation is due
     */
    Long daysUntilRotation,

    /**
     * Last rotation timestamp
     */
    Instant lastRotatedAt,

    /**
     * Whether rotation is required (past due date)
     */
    Boolean rotationRequired,

    /**
     * Whether the principal has an API key (SERVICE principals)
     */
    Boolean hasApiKey,

    /**
     * Whether the principal has a certificate (SYSTEM principals)
     */
    Boolean hasCertificate
) {
}
