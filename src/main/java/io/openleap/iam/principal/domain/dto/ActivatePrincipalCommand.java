package io.openleap.iam.principal.domain.dto;

import java.util.UUID;

/**
 * Command for activating a principal.
 * 
 * Supports both email verification (self-activation) and admin override.
 */
public record ActivatePrincipalCommand(
    /**
     * Principal ID to activate
     */
    UUID id,
    
    /**
     * Email verification token (for self-activation)
     */
    String verificationToken,
    
    /**
     * Admin override flag (for admin activation)
     */
    Boolean adminOverride,
    
    /**
     * Reason for admin activation (optional)
     */
    String reason
) {
}
