package io.openleap.iam.principal.domain.event;

import java.util.UUID;

/**
 * Event payload for iam.principal.principal.activated event.
 * 
 * Published when a principal is successfully activated.
 * 
 * See spec/iam_principal_spec.md Section 5.1 - iam.principal.principal.activated
 */
public record PrincipalActivatedEvent(
    /**
     * Principal unique identifier
     */
    UUID principalId,
    
    /**
     * Principal type (HUMAN, SERVICE, SYSTEM, DEVICE)
     */
    String principalType,
    
    /**
     * Principal status (should be ACTIVE)
     */
    String status,
    
    /**
     * Who activated the principal (self, admin)
     */
    String activatedBy,
    
    /**
     * Activation method (email_verification, admin_override)
     */
    String activationMethod
) {
}
