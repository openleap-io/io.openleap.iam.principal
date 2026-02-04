package io.openleap.iam.principal.domain.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Command for deactivating a principal.
 */
public record DeactivatePrincipalCommand(
    /**
     * Principal ID to deactivate
     */
    UUID id,

    /**
     * Reason for deactivation (required)
     */
    String reason,

    /**
     * Effective date for deactivation (optional)
     */
    LocalDate effectiveDate
) {
}
