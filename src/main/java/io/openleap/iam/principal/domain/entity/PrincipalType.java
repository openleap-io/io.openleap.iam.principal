package io.openleap.iam.principal.domain.entity;

public enum PrincipalType {
    /**
     * Human user principal
     */
    HUMAN,
    
    /**
     * Service/microservice principal
     */
    SERVICE,
    
    /**
     * External system principal
     */
    SYSTEM,
    
    /**
     * Device/IoT principal
     */
    DEVICE
}

