package io.openleap.iam.principal.domain.dto;

import java.util.List;

/**
 * Result of cross-tenant principal search.
 */
public record CrossTenantSearchResult(
    /**
     * List of principal items
     */
    List<CrossTenantPrincipalItem> items,

    /**
     * Total number of matching principals
     */
    long total,

    /**
     * Current page number
     */
    int page,

    /**
     * Page size
     */
    int size
) {
}
