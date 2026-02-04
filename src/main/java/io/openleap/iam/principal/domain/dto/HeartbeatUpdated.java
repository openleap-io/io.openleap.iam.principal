package io.openleap.iam.principal.domain.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Result of updating a device principal's heartbeat.
 */
public record HeartbeatUpdated(
    /**
     * Device principal ID
     */
    UUID id,

    /**
     * Timestamp of the heartbeat
     */
    Instant lastHeartbeatAt
) {
}
