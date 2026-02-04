package io.openleap.iam.principal.domain.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Domain DTO representing a successfully created service principal.
 * Used by the service layer.
 * 
 * Note: Contains sensitive credentials (API key and client secret) that are
 * returned only once during creation.
 */
public record ServicePrincipalCreated(
    UUID id,
    String username,
    String serviceName,
    String apiKey,
    String keycloakClientId,
    String keycloakClientSecret,
    List<String> allowedScopes,
    LocalDate credentialRotationDate
) {
}
