package io.openleap.iam.principal.exception;

public class ServiceNameAlreadyExistsException extends RuntimeException {
    
    public ServiceNameAlreadyExistsException(String serviceName) {
        super("Service name already exists: " + serviceName);
    }
}
