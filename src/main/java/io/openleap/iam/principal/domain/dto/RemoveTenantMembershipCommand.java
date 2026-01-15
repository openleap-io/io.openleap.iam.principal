package io.openleap.iam.principal.domain.dto;

import java.util.UUID;

/**
 * Command for removing a tenant membership from a principal.
 */
public record RemoveTenantMembershipCommand(
    /**
     * Principal ID to remove membership from
     */
    UUID principalId,

    /**
     * Tenant ID to remove membership for
     */
    UUID tenantId
) {
}
