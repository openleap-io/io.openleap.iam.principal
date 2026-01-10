package io.openleap.iam.principal.domain.event;

import java.util.UUID;

/**
 * Event payload for iam.principal.principal.created event.
 * 
 * Published when a new principal is successfully created and synced to Keycloak.
 * 
 * See spec/iam_principal_spec.md Section 5.1 - iam.principal.principal.created
 */
public record PrincipalCreatedEvent(
    /**
     * Principal unique identifier
     */
    UUID principalId,
    
    /**
     * Principal type (HUMAN, SERVICE, SYSTEM, DEVICE)
     */
    String principalType,
    
    /**
     * Login username (globally unique)
     */
    String username,
    
    /**
     * Email address (required for HUMAN, optional for others)
     */
    String email,
    
    /**
     * Primary tenant ID
     */
    UUID primaryTenantId,
    
    /**
     * Principal status (PENDING, ACTIVE, SUSPENDED, INACTIVE, DELETED)
     */
    String status,
    
    /**
     * Creator principal ID (nullable)
     */
    UUID createdBy
) {
}
