package io.openleap.iam.principal.domain.dto;

import java.util.UUID;

/**
 * Command for suspending a principal.
 */
public record SuspendPrincipalCommand(
    /**
     * Principal ID to suspend
     */
    UUID principalId,
    
    /**
     * Reason for suspension (required)
     */
    String reason,
    
    /**
     * Incident ticket reference (optional)
     */
    String incidentTicket
) {
}
