package io.openleap.iam.principal.domain.dto;

import java.util.UUID;

/**
 * A single principal item in cross-tenant search results.
 */
public record CrossTenantPrincipalItem(
    /**
     * Principal ID
     */
    UUID id,

    /**
     * Principal type (HUMAN, SERVICE, SYSTEM, DEVICE)
     */
    String principalType,

    /**
     * Username
     */
    String username,

    /**
     * Email address
     */
    String email,

    /**
     * Principal status
     */
    String status,

    /**
     * Primary tenant ID
     */
    UUID defaultTenantId
) {
}
