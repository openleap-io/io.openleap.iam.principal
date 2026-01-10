package io.openleap.iam.principal.domain.dto;

import java.util.Map;
import java.util.UUID;

/**
 * Domain DTO for creating a human principal.
 * Used by the service layer.
 */
public record CreateHumanPrincipalCommand(
    String username,
    String email,
    UUID primaryTenantId,
    Map<String, Object> contextTags,
    String displayName,
    String phone,
    String language,
    String timezone,
    String locale,
    String avatarUrl,
    String bio,
    Map<String, Object> preferences
) {
}

