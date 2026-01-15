package io.openleap.iam.principal.domain.dto;

import java.util.Map;
import java.util.UUID;

/**
 * Command for updating a device principal's heartbeat.
 */
public record UpdateHeartbeatCommand(
    /**
     * Device principal ID
     */
    UUID principalId,

    /**
     * Optional firmware version update
     */
    String firmwareVersion,

    /**
     * Optional location info update
     */
    Map<String, Object> locationInfo
) {
}
