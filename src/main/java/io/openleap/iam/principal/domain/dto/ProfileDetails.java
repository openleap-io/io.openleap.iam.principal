package io.openleap.iam.principal.domain.dto;

import java.util.Map;
import java.util.UUID;

/**
 * Result containing profile details for a human principal.
 */
public record ProfileDetails(
    /**
     * Principal ID
     */
    UUID id,

    /**
     * First name
     */
    String firstName,

    /**
     * Last name
     */
    String lastName,

    /**
     * Display name
     */
    String displayName,

    /**
     * Phone number (E.164 format)
     */
    String phone,

    /**
     * Preferred language (ISO 639-1)
     */
    String language,

    /**
     * Timezone (IANA timezone)
     */
    String timezone,

    /**
     * Locale (BCP 47)
     */
    String locale,

    /**
     * Profile picture URL
     */
    String avatarUrl,

    /**
     * Biography
     */
    String bio,

    /**
     * UI preferences
     */
    Map<String, Object> preferences
) {
}
