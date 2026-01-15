package io.openleap.iam.principal.domain.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a single tenant membership item.
 */
public record TenantMembershipItem(
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
     * Membership end date (null = no expiry)
     */
    LocalDate validTo,

    /**
     * Membership status
     */
    String status,

    /**
     * Whether this is the primary tenant
     */
    Boolean isPrimary
) {
}
