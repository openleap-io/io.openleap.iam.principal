package io.openleap.iam.principal.domain.entity;

public enum SyncStatus {
    /**
     * Not yet synchronized to Keycloak
     */
    PENDING,
    
    /**
     * Currently synchronizing
     */
    SYNCING,
    
    /**
     * Successfully synchronized
     */
    SYNCED,
    
    /**
     * Synchronization failed
     */
    FAILED
}

