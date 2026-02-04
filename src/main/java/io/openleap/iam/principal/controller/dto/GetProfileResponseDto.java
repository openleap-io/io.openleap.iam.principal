package io.openleap.iam.principal.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Response DTO for getting a human principal's profile.
 */
public class GetProfileResponseDto {

    /**
     * Principal ID
     */
    @JsonProperty("id")
    private String id;

    /**
     * First name
     */
    @JsonProperty("first_name")
    private String firstName;

    /**
     * Last name
     */
    @JsonProperty("last_name")
    private String lastName;

    /**
     * Display name
     */
    @JsonProperty("display_name")
    private String displayName;

    /**
     * Phone number (E.164 format)
     */
    @JsonProperty("phone")
    private String phone;

    /**
     * Preferred language (ISO 639-1)
     */
    @JsonProperty("language")
    private String language;

    /**
     * Timezone (IANA timezone)
     */
    @JsonProperty("timezone")
    private String timezone;

    /**
     * Locale (BCP 47)
     */
    @JsonProperty("locale")
    private String locale;

    /**
     * Profile picture URL
     */
    @JsonProperty("avatar_url")
    private String avatarUrl;

    /**
     * Biography
     */
    @JsonProperty("bio")
    private String bio;

    /**
     * UI preferences
     */
    @JsonProperty("preferences")
    private Map<String, Object> preferences;

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Map<String, Object> getPreferences() {
        return preferences;
    }

    public void setPreferences(Map<String, Object> preferences) {
        this.preferences = preferences;
    }
}
