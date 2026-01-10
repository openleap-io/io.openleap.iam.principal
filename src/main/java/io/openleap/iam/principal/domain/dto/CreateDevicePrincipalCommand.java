package io.openleap.iam.principal.domain.dto;

import io.openleap.iam.principal.domain.entity.DeviceType;

import java.util.Map;
import java.util.UUID;

/**
 * Domain DTO for creating a device principal.
 * Used by the service layer.
 */
public record CreateDevicePrincipalCommand(
    String deviceIdentifier,
    DeviceType deviceType,
    UUID primaryTenantId,
    String manufacturer,
    String model,
    String firmwareVersion,
    String certificateThumbprint,
    Map<String, Object> locationInfo,
    Map<String, Object> contextTags
) {
}
