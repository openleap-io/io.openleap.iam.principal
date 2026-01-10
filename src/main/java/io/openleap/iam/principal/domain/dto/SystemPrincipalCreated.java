package io.openleap.iam.principal.domain.dto;

import java.util.List;
import java.util.UUID;

/**
 * Domain DTO representing a successfully created system principal.
 * Used by the service layer.
 */
public record SystemPrincipalCreated(
    UUID principalId,
    String username,
    String systemIdentifier,
    String integrationType,
    String certificateThumbprint,
    List<String> allowedOperations
) {
}
