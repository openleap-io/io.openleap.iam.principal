package io.openleap.iam.principal.domain.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Result DTO for principal GDPR deletion.
 */
public record PrincipalDeleted(
    /**
     * Principal ID
     */
    UUID id,

    /**
     * Whether the principal data was anonymized
     */
    boolean anonymized,

    /**
     * Audit reference for tracking
     */
    String auditReference,

    /**
     * Timestamp when the principal was deleted
     */
    Instant deletedAt
) {
}
