package io.openleap.iam.principal.domain.event;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Event payload for iam.principal.service_principal.created event.
 * 
 * Published when a new service principal is successfully created and synced to Keycloak.
 * 
 * See spec/iam_principal_spec.md Section 5.1 - iam.principal.service_principal.created
 */
public record ServicePrincipalCreatedEvent(
    /**
     * Principal unique identifier
     */
    UUID principalId,
    
    /**
     * Principal type (SERVICE)
     */
    String principalType,
    
    /**
     * Login username (globally unique)
     */
    String username,
    
    /**
     * Service name (globally unique)
     */
    String serviceName,
    
    /**
     * Primary tenant ID
     */
    UUID primaryTenantId,
    
    /**
     * Principal status (ACTIVE)
     */
    String status,
    
    /**
     * OAuth2 scopes allowed for this service
     */
    List<String> allowedScopes,
    
    /**
     * Next credential rotation date
     */
    LocalDate credentialRotationDate,
    
    /**
     * Creator principal ID (nullable)
     */
    UUID createdBy
) {
}
