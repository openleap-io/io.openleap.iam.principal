package io.openleap.iam.principal.exception;

public class SystemIdentifierAlreadyExistsException extends RuntimeException {
    
    public SystemIdentifierAlreadyExistsException(String systemIdentifier) {
        super("System identifier already exists: " + systemIdentifier);
    }
}
