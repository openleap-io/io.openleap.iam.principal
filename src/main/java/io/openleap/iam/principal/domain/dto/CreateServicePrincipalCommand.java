package io.openleap.iam.principal.domain.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Domain DTO for creating a service principal.
 * Used by the service layer.
 */
public record CreateServicePrincipalCommand(
    String serviceName,
    UUID defaultTenantId,
    Map<String, Object> contextTags,
    List<String> allowedScopes
) {
}
