package io.openleap.iam.principal.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "human_principals", schema = "iam_principal")
public class HumanPrincipalEntity extends Principal {
    
    /**
     * Keycloak user ID (UK, nullable, set after sync)
     */
    @Column(name = "keycloak_user_id", unique = true, length = 255)
    private String keycloakUserId;
    
    /**
     * Email verified flag
     */
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;
    
    /**
     * MFA enrolled flag
     */
    @Column(name = "mfa_enabled", nullable = false)
    private Boolean mfaEnabled = false;
    
    /**
     * Last authentication timestamp
     */
    @Column(name = "last_login_at")
    private Instant lastLoginAt;
    
    /**
     * Preferred display name (required, max 200 chars)
     */
    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName;
    
    /**
     * Phone number (optional, E.164 format)
     */
    @Column(name = "phone", length = 20)
    private String phone;
    
    /**
     * Preferred language (ISO 639-1, e.g., 'en', 'de')
     */
    @Column(name = "language", length = 10)
    private String language;
    
    /**
     * Timezone (IANA timezone, e.g., 'America/New_York')
     */
    @Column(name = "timezone", length = 100)
    private String timezone;
    
    /**
     * Locale (BCP 47, e.g., 'en-US', 'de-DE')
     */
    @Column(name = "locale", length = 20)
    private String locale;
    
    /**
     * Profile picture URL (DMS reference, max 500 chars)
     */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;
    
    /**
     * Biography (max 2000 chars)
     */
    @Column(name = "bio", length = 2000)
    private String bio;
    
    /**
     * UI preferences (application-specific JSONB)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferences", columnDefinition = "jsonb")
    private Map<String, Object> preferences;
    
    @Override
    public PrincipalType getPrincipalType() {
        return PrincipalType.HUMAN;
    }
    
    // Getters and Setters
    
    public String getKeycloakUserId() {
        return keycloakUserId;
    }
    
    public void setKeycloakUserId(String keycloakUserId) {
        this.keycloakUserId = keycloakUserId;
    }
    
    public Boolean getEmailVerified() {
        return emailVerified;
    }
    
    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    
    public Boolean getMfaEnabled() {
        return mfaEnabled;
    }
    
    public void setMfaEnabled(Boolean mfaEnabled) {
        this.mfaEnabled = mfaEnabled;
    }
    
    public Instant getLastLoginAt() {
        return lastLoginAt;
    }
    
    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
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

