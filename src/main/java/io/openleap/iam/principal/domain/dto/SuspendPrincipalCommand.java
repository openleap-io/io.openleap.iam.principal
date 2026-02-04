package io.openleap.iam.principal.domain.dto;

import java.util.UUID;

/**
 * Command for suspending a principal.
 */
public record SuspendPrincipalCommand(
    /**
     * Principal ID to suspend
     */
    UUID id,
    
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
