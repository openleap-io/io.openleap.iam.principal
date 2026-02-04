package io.openleap.iam.principal.domain.dto;

import io.openleap.iam.principal.domain.entity.IntegrationType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Domain DTO for creating a system principal.
 * Used by the service layer.
 */
public record CreateSystemPrincipalCommand(
    String systemIdentifier,
    IntegrationType integrationType,
    UUID defaultTenantId,
    String certificateThumbprint,
    Map<String, Object> contextTags,
    List<String> allowedOperations
) {
}
