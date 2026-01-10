package io.openleap.iam.principal.service;

import java.util.UUID;

/**
 * Service interface for tenant validation.
 * This is a placeholder that should be implemented to call the tenant service.
 */
public interface TenantService {
    
    /**
     * Check if a tenant exists.
     * 
     * @param tenantId the tenant ID to check
     * @return true if the tenant exists, false otherwise
     */
    boolean tenantExists(UUID tenantId);
}

