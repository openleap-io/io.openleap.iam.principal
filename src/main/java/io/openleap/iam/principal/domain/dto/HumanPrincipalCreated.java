package io.openleap.iam.principal.domain.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Domain DTO representing a successfully created human principal.
 * Used by the service layer.
 */
public record HumanPrincipalCreated(
    // businessId maps to "id"
    UUID id,

    // Principal base fields
    String principalType,
    String username,
    String email,
    UUID defaultTenantId,
    String status,
    Map<String, Object> contextTags,
    String syncStatus,
    Instant createdAt,
    Instant updatedAt,

    // HumanPrincipalEntity specific fields
    String keycloakUserId,
    Boolean emailVerified,
    Boolean mfaEnabled,
    Instant lastLoginAt,
    String displayName,
    String firstName,
    String lastName,
    String phone,
    String language,
    String timezone,
    String locale,
    String avatarUrl,
    String bio,
    Map<String, Object> preferences
) {
}
