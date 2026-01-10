package io.openleap.iam.principal.domain.event;

import java.util.List;
import java.util.UUID;

/**
 * Event payload for iam.principal.system_principal.created event.
 * 
 * Published when a new system principal is successfully created.
 * 
 * See spec/iam_principal_spec.md Section 5.1 - iam.principal.system_principal.created
 */
public record SystemPrincipalCreatedEvent(
    /**
     * Principal unique identifier
     */
    UUID principalId,
    
    /**
     * Principal type (SYSTEM)
     */
    String principalType,
    
    /**
     * Login username (globally unique)
     */
    String username,
    
    /**
     * System identifier (globally unique)
     */
    String systemIdentifier,
    
    /**
     * Integration type (ERP, CRM, EXTERNAL_API, PARTNER)
     */
    String integrationType,
    
    /**
     * Primary tenant ID
     */
    UUID primaryTenantId,
    
    /**
     * Principal status (ACTIVE)
     */
    String status,
    
    /**
     * Permitted operations (whitelist)
     */
    List<String> allowedOperations,
    
    /**
     * Creator principal ID (nullable)
     */
    UUID createdBy
) {
}
