package io.openleap.iam.principal.service.keycloak.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record User(
        String id,
        @NotBlank(message = "Username is required") String username,
        @Email(message = "Email must be valid") String email,
        @NotBlank(message = "First name is required") String firstName,
        @NotBlank(message = "Last name is required") String lastName,
        Boolean enabled,
        boolean emailVerified,
        List<String> roles,
        @NotBlank String organizationName
) {

    public User(String id, String username, String email, String firstName, String lastName, List<String> roles, String organizationName) {
        this(id, username, email, firstName, lastName, true, false, roles, organizationName);
    }

    // Builder pattern for complex construction
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private Boolean enabled = true;
        private boolean emailVerified = false;
        private List<String> roles;
        private String organizationName;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder enabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder emailVerified(boolean emailVerified) {
            this.emailVerified = emailVerified;
            return this;
        }

        public Builder roles(List<String> roles) {
            this.roles = roles;
            return this;
        }

        public Builder organizationName(String organizationName) {
            this.organizationName = organizationName;
            return this;
        }

        public User build() {
            return new User(id, username, email, firstName, lastName, enabled, emailVerified
                    , roles, organizationName);
        }
    }
}
