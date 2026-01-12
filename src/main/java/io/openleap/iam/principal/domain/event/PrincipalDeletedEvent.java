package io.openleap.iam.principal.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Event payload for iam.principal.principal.deleted event.
 *
 * Published when a principal is successfully deleted per GDPR requirements.
 *
 * See spec/iam_principal_spec.md Section 5.1 - iam.principal.principal.deleted
 */
public record PrincipalDeletedEvent(
    /**
     * Pseudonymized principal identifier (for audit trail)
     */
    UUID principalId,

    /**
     * Principal type (HUMAN, SERVICE, SYSTEM, DEVICE)
     */
    String principalType,

    /**
     * GDPR request ticket reference
     */
    String gdprRequestTicket,

    /**
     * Email of the requestor
     */
    String requestorEmail,

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
