package io.openleap.iam.principal.domain.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Domain DTO for principal details.
 */
public record PrincipalDetails(
    UUID id,
    String principalType,
    String username,
    String email,
    String status,
    UUID defaultTenantId,
    Instant createdAt,
    Instant updatedAt,
    Map<String, Object> contextTags,

    // Human principal specific fields
    Boolean emailVerified,
    Boolean mfaEnabled,
    Instant lastLoginAt,
    String displayName,
    String firstName,
    String lastName,

    // Service principal specific fields
    String serviceName,
    List<String> allowedScopes,
    LocalDate credentialRotationDate,

    // System principal specific fields
    String systemIdentifier,
    String integrationType,
    List<String> allowedOperations,

    // Device principal specific fields
    String deviceIdentifier,
    String deviceType,
    String manufacturer,
    String model
) {
}
