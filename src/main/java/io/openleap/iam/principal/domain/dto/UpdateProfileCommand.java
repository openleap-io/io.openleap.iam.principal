package io.openleap.iam.principal.domain.dto;

import java.util.Map;
import java.util.UUID;

/**
 * Domain DTO for updating a human principal profile.
 * Used by the service layer.
 */
public record UpdateProfileCommand(
    UUID id,
    String firstName,
    String lastName,
    String displayName,
    String phone,
    String language,
    String timezone,
    String locale,
    String avatarUrl,
    String bio,
    Map<String, Object> preferences,
    Map<String, Object> contextTags
) {
}
