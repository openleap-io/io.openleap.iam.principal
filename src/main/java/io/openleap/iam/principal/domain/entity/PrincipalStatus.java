package io.openleap.iam.principal.domain.entity;

public enum PrincipalStatus {
    /**
     * Just created, awaiting activation
     */
    PENDING,
    
    /**
     * Operational and can authenticate
     */
    ACTIVE,
    
    /**
     * Temporarily disabled (security incident, under review)
     */
    SUSPENDED,
    
    /**
     * Deactivated (principal removed from organization)
     */
    INACTIVE,
    
    /**
     * Purged (GDPR deletion, final state)
     */
    DELETED
}

