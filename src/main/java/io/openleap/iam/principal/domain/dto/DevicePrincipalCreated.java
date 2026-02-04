package io.openleap.iam.principal.domain.dto;

import io.openleap.iam.principal.domain.entity.DeviceType;

import java.util.UUID;

/**
 * Domain DTO representing a successfully created device principal.
 * Used by the service layer.
 */
public record DevicePrincipalCreated(
    UUID id,
    String username,
    String deviceIdentifier,
    DeviceType deviceType,
    String manufacturer,
    String model,
    String certificateThumbprint
) {
}
