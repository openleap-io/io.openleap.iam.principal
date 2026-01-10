package io.openleap.iam.principal.exception;

public class DeviceIdentifierAlreadyExistsException extends RuntimeException {
    
    public DeviceIdentifierAlreadyExistsException(String deviceIdentifier) {
        super("Device identifier already exists: " + deviceIdentifier);
    }
}
