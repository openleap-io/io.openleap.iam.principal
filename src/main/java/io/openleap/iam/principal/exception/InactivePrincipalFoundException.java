package io.openleap.iam.principal.exception;

import java.util.UUID;

public class InactivePrincipalFoundException extends RuntimeException {
    
    private final UUID principalId;
    
    public InactivePrincipalFoundException(UUID principalId, String email) {
        super("Inactive principal found with email: " + email + " (principal_id: " + principalId + ")");
        this.principalId = principalId;
    }
    
    public UUID getPrincipalId() {
        return principalId;
    }
}

