package io.openleap.iam.principal.domain.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Result of adding a tenant membership.
 */
public record TenantMembershipAdded(
    /**
     * Membership ID
     */
    UUID id,

    /**
     * Principal ID
     */
    UUID principalId,

    /**
     * Tenant ID
     */
    UUID tenantId,

    /**
     * Membership start date
     */
    LocalDate validFrom,

    /**
     * Membership end date
     */
    LocalDate validTo,

    /**
     * Membership status
     */
    String status,

    /**
     * Principal ID of the user who added the membership
     */
    UUID invitedBy,

    /**
     * Timestamp when the membership was created
     */
    Instant createdAt
) {
}
