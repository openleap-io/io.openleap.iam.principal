package io.openleap.iam.principal.domain.event;

import java.util.Map;
import java.util.UUID;

/**
 * Event payload for iam.principal.device_principal.created event.
 * 
 * Published when a new device principal is successfully created.
 * 
 * See spec/iam_principal_spec.md Section 5.1 - iam.principal.device_principal.created
 */
public record DevicePrincipalCreatedEvent(
    /**
     * Principal unique identifier
     */
    UUID principalId,
    
    /**
     * Principal type (DEVICE)
     */
    String principalType,
    
    /**
     * Login username (globally unique)
     */
    String username,
    
    /**
     * Device identifier (globally unique)
     */
    String deviceIdentifier,
    
    /**
     * Device type (IOT_SENSOR, EDGE_DEVICE, KIOSK, TERMINAL, GATEWAY, OTHER)
     */
    String deviceType,
    
    /**
     * Primary tenant ID
     */
    UUID primaryTenantId,
    
    /**
     * Principal status (ACTIVE)
     */
    String status,
    
    /**
     * Device manufacturer (optional)
     */
    String manufacturer,
    
    /**
     * Device model (optional)
     */
    String model,
    
    /**
     * Device firmware version (optional)
     */
    String firmwareVersion,
    
    /**
     * Certificate thumbprint
     */
    String certificateThumbprint,
    
    /**
     * Location information (optional)
     */
    Map<String, Object> locationInfo,
    
    /**
     * Creator principal ID (nullable)
     */
    UUID createdBy
) {
}
