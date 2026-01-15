package io.openleap.iam.principal.domain.dto;

/**
 * Query for cross-tenant principal search.
 */
public record CrossTenantSearchQuery(
    /**
     * Search term for username or email
     */
    String search,

    /**
     * Filter by principal type (HUMAN, SERVICE, SYSTEM, DEVICE)
     */
    String principalType,

    /**
     * Filter by status (PENDING, ACTIVE, SUSPENDED, DEACTIVATED)
     */
    String status,

    /**
     * Page number (1-indexed)
     */
    int page,

    /**
     * Page size
     */
    int size
) {
}
