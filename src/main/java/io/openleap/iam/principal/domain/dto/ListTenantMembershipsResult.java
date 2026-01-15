package io.openleap.iam.principal.domain.dto;

import java.util.List;

/**
 * Result containing paginated list of tenant memberships.
 */
public record ListTenantMembershipsResult(
    /**
     * List of membership items
     */
    List<TenantMembershipItem> items,

    /**
     * Total number of memberships
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
}
