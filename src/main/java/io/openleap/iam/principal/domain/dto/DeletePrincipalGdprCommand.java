package io.openleap.iam.principal.domain.dto;

import java.util.UUID;

/**
 * Command for GDPR principal deletion.
 */
public record DeletePrincipalGdprCommand(
    /**
     * Principal ID to delete
     */
    UUID principalId,

    /**
     * Confirmation string (must be "DELETE")
     */
    String confirmation,

    /**
     * GDPR request ticket reference
     */
    String gdprRequestTicket,

    /**
     * Email of the requestor
     */
    String requestorEmail
) {
}
