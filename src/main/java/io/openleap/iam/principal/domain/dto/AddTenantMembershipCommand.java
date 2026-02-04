package io.openleap.iam.principal.domain.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Command for adding a tenant membership to a principal.
 */
public record AddTenantMembershipCommand(
    /**
     * Principal ID to add membership for
     */
    UUID id,

    /**
     * Tenant ID to add membership for
     */
    UUID tenantId,

    /**
     * Membership start date (optional, defaults to today)
     */
    LocalDate validFrom,

    /**
     * Membership end date (optional, null = no expiry)
     */
    LocalDate validTo,

    /**
     * Principal ID of the user adding the membership
     */
    UUID invitedBy
) {
}
