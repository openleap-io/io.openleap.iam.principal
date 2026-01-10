package io.openleap.iam.principal.controller.exception;

import io.openleap.iam.principal.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class PrincipalExceptionHandler {

    @ExceptionHandler(ServiceNameAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleServiceNameAlreadyExistsException(ServiceNameAlreadyExistsException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "ServiceNameAlreadyExistsException");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "UsernameAlreadyExists");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
    
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "EmailAlreadyExists");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
    
    @ExceptionHandler(TenantNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleTenantNotFound(TenantNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "TenantNotFound");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
    
    @ExceptionHandler(InactivePrincipalFoundException.class)
    public ResponseEntity<Map<String, Object>> handleInactivePrincipalFound(InactivePrincipalFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "InactivePrincipalFound");
        body.put("message", ex.getMessage());
        body.put("principalId", ex.getPrincipalId());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
    
    @ExceptionHandler(SystemIdentifierAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleSystemIdentifierAlreadyExists(SystemIdentifierAlreadyExistsException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "SystemIdentifierAlreadyExists");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
    
    @ExceptionHandler(DeviceIdentifierAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleDeviceIdentifierAlreadyExists(DeviceIdentifierAlreadyExistsException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "DeviceIdentifierAlreadyExists");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        body.put("error", "ValidationFailed");
        body.put("message", "Request validation failed");
        body.put("errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}

