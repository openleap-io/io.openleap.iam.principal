package io.openleap.iam.principal.domain.event;

import java.util.UUID;

/**
 * Event payload for iam.principal.principal.suspended event.
 * 
 * Published when a principal is successfully suspended.
 * 
 * See spec/iam_principal_spec.md Section 5.1 - iam.principal.principal.suspended
 */
public record PrincipalSuspendedEvent(
    /**
     * Principal unique identifier
     */
    UUID principalId,
    
    /**
     * Principal type (HUMAN, SERVICE, SYSTEM, DEVICE)
     */
    String principalType,
    
    /**
     * Principal status (should be SUSPENDED)
     */
    String status,
    
    /**
     * Reason for suspension
     */
    String reason,
    
    /**
     * Incident ticket reference (optional)
     */
    String incidentTicket
) {
}
