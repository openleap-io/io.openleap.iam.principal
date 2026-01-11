package io.openleap.iam.principal.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * Request DTO for updating a human principal profile.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateProfileRequestDto {
    
    @Size(max = 200, message = "First name must not exceed 200 characters")
    private String firstName;
    
    @Size(max = 200, message = "Last name must not exceed 200 characters")
    private String lastName;
    
    @Size(max = 200, message = "Display name must not exceed 200 characters")
    private String displayName;
    
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;
    
    @Size(max = 10, message = "Language must not exceed 10 characters")
    private String language;
    
    @Size(max = 100, message = "Timezone must not exceed 100 characters")
    private String timezone;
    
    @Size(max = 20, message = "Locale must not exceed 20 characters")
    private String locale;
    
    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    private String avatarUrl;
    
    @Size(max = 2000, message = "Bio must not exceed 2000 characters")
    private String bio;
    
    private Map<String, Object> preferences;
    
    private Map<String, Object> contextTags;
    
    // Getters and Setters
    
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
    
    public Map<String, Object> getContextTags() {
        return contextTags;
    }
    
    public void setContextTags(Map<String, Object> contextTags) {
        this.contextTags = contextTags;
    }
}
