package io.openleap.iam.principal.domain.entity;

import io.openleap.common.domain.BusinessId;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;

/**
 * Typed identifier for Policy entities.
 */
@Embeddable
public record PrincipalId(
        @Column(name = "business_id", nullable = false, updatable = false)
        UUID value
) implements BusinessId {

    public PrincipalId {
        if (value == null) {
            throw new IllegalArgumentException("PolicyId value cannot be null");
        }
    }

    public static PrincipalId create() {
        return new PrincipalId(UUID.randomUUID());
    }

    public static PrincipalId of(UUID value) {
        return new PrincipalId(value);
    }

    public static PrincipalId parse(String value) {
        return new PrincipalId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
