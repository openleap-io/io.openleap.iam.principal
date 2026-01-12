package io.openleap.iam.principal.domain.dto;

import io.openleap.iam.principal.domain.entity.PrincipalStatus;
import io.openleap.iam.principal.domain.entity.PrincipalType;

import java.util.UUID;

/**
 * Query parameters for searching principals.
 */
public record SearchPrincipalsQuery(
    /**
     * Search term (partial match on username or email)
     */
    String search,

    /**
     * Filter by principal type
     */
    PrincipalType principalType,

    /**
     * Filter by status
     */
    PrincipalStatus status,

    /**
     * Filter by tenant ID
     */
    UUID tenantId,

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
