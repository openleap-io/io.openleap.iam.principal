package io.openleap.iam.principal.domain.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Result DTO for service principal credentials rotation.
 */
public record CredentialsRotated(
    /**
     * Principal ID
     */
    UUID id,

    /**
     * New API key (returned only once)
     */
    String apiKey,

    /**
     * New Keycloak client secret (returned only once)
     */
    String keycloakClientSecret,

    /**
     * Next rotation date
     */
    LocalDate credentialRotationDate,

    /**
     * Timestamp when credentials were rotated
     */
    Instant rotatedAt
) {
}
