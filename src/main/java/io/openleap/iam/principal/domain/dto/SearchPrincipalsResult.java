package io.openleap.iam.principal.domain.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Result of searching principals.
 */
public record SearchPrincipalsResult(
    /**
     * List of matching principals
     */
    List<PrincipalItem> items,

    /**
     * Total count of matching principals
     */
    long total,

    /**
     * Current page number (1-indexed)
     */
    int page,

    /**
     * Page size
     */
    int size
) {
    /**
     * Item representing a principal in search results.
     */
    public record PrincipalItem(
        UUID principalId,
        String username,
        String email,
        String principalType,
        String status,
        UUID primaryTenantId,
        Instant lastLoginAt,
        Instant createdAt
    ) {
    }
}
