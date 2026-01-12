package io.openleap.iam.principal.domain.event;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Event payload for iam.principal.principal.deactivated event.
 *
 * Published when a principal is successfully deactivated.
 *
 * See spec/iam_principal_spec.md Section 5.1 - iam.principal.principal.deactivated
 */
public record PrincipalDeactivatedEvent(
    /**
     * Principal unique identifier
     */
    UUID principalId,

    /**
     * Principal type (HUMAN, SERVICE, SYSTEM, DEVICE)
     */
    String principalType,

    /**
     * Principal status (should be INACTIVE)
     */
    String status,

    /**
     * Reason for deactivation
     */
    String reason,

    /**
     * Effective date for deactivation
     */
    LocalDate effectiveDate
) {
}
