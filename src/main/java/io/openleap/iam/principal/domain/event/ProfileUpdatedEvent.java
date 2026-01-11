package io.openleap.iam.principal.domain.event;

import java.util.List;
import java.util.UUID;

/**
 * Event payload for iam.principal.profile.updated event.
 * 
 * Published when a human principal profile is successfully updated.
 * 
 * See spec/iam_principal_spec.md Section 5.1 - iam.principal.profile.updated
 */
public record ProfileUpdatedEvent(
    /**
     * Principal unique identifier
     */
    UUID principalId,
    
    /**
     * Principal ID of the user who made the update (self-update or admin)
     */
    UUID updatedBy,
    
    /**
     * List of field names that were changed
     */
    List<String> changedFields
) {
}
