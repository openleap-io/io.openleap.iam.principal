package io.openleap.iam.principal.domain.dto;

import java.util.List;
import java.util.UUID;

/**
 * Domain DTO for profile update result.
 */
public record ProfileUpdated(
    UUID principalId,
    List<String> changedFields
) {
}
