package io.openleap.iam.principal.exception;

public class UserAlreadyExistsException extends RuntimeException {
    private final String email;

    public UserAlreadyExistsException(String email) {
        super("User not found: " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
