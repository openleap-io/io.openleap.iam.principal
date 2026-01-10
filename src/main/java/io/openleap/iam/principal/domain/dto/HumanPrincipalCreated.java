package io.openleap.iam.principal.domain.dto;

import java.util.UUID;

/**
 * Domain DTO representing a successfully created human principal.
 * Used by the service layer.
 */
public record HumanPrincipalCreated(
    UUID principalId
) {
}

