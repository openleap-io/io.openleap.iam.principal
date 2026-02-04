package io.openleap.iam.principal.controller.exception;

import io.openleap.iam.principal.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("PrincipalExceptionHandler Unit Tests")
class PrincipalExceptionHandlerTest {

    private PrincipalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new PrincipalExceptionHandler();
    }

    @Nested
    @DisplayName("handleServiceNameAlreadyExistsException")
    class HandleServiceNameAlreadyExists {

        @Test
        @DisplayName("should return CONFLICT status with correct error details")
        void shouldReturnConflictStatus() {
            // given
            ServiceNameAlreadyExistsException exception = new ServiceNameAlreadyExistsException("PaymentService");

            // when
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleServiceNameAlreadyExistsException(exception);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).containsEntry("error", "ServiceNameAlreadyExistsException");
            assertThat(response.getBody().get("message")).asString().contains("PaymentService");
        }
    }

    @Nested
    @DisplayName("handleUsernameAlreadyExists")
    class HandleUsernameAlreadyExists {

        @Test
        @DisplayName("should return CONFLICT status with correct error details")
        void shouldReturnConflictStatus() {
            // given
            UsernameAlreadyExistsException exception = new UsernameAlreadyExistsException("johndoe");

            // when
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleUsernameAlreadyExists(exception);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).containsEntry("error", "UsernameAlreadyExists");
            assertThat(response.getBody().get("message")).asString().contains("johndoe");
        }
    }

    @Nested
    @DisplayName("handleEmailAlreadyExists")
    class HandleEmailAlreadyExists {

        @Test
        @DisplayName("should return CONFLICT status with correct error details")
        void shouldReturnConflictStatus() {
            // given
            EmailAlreadyExistsException exception = new EmailAlreadyExistsException("john@example.com");

            // when
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleEmailAlreadyExists(exception);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).containsEntry("error", "EmailAlreadyExists");
            assertThat(response.getBody().get("message")).asString().contains("john@example.com");
        }
    }

    @Nested
    @DisplayName("handleTenantNotFound")
    class HandleTenantNotFound {

        @Test
        @DisplayName("should return NOT_FOUND status with correct error details")
        void shouldReturnNotFoundStatus() {
            // given
            UUID tenantId = UUID.randomUUID();
            TenantNotFoundException exception = new TenantNotFoundException(tenantId);

            // when
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleTenantNotFound(exception);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).containsEntry("error", "TenantNotFound");
            assertThat(response.getBody().get("message")).asString().contains(tenantId.toString());
        }
    }

    @Nested
    @DisplayName("handleInactivePrincipalFound")
    class HandleInactivePrincipalFound {

        @Test
        @DisplayName("should return CONFLICT status with principal ID")
        void shouldReturnConflictStatusWithPrincipalId() {
            // given
            UUID principalId = UUID.randomUUID();
            InactivePrincipalFoundException exception = new InactivePrincipalFoundException(principalId, "john@example.com");

            // when
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInactivePrincipalFound(exception);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).containsEntry("error", "InactivePrincipalFound");
            assertThat(response.getBody()).containsEntry("id", principalId);
            assertThat(response.getBody().get("message")).asString().contains("john@example.com");
        }
    }

    @Nested
    @DisplayName("handleSystemIdentifierAlreadyExists")
    class HandleSystemIdentifierAlreadyExists {

        @Test
        @DisplayName("should return CONFLICT status with correct error details")
        void shouldReturnConflictStatus() {
            // given
            SystemIdentifierAlreadyExistsException exception = new SystemIdentifierAlreadyExistsException("ERP_SYSTEM_001");

            // when
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleSystemIdentifierAlreadyExists(exception);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).containsEntry("error", "SystemIdentifierAlreadyExists");
            assertThat(response.getBody().get("message")).asString().contains("ERP_SYSTEM_001");
        }
    }

    @Nested
    @DisplayName("handleDeviceIdentifierAlreadyExists")
    class HandleDeviceIdentifierAlreadyExists {

        @Test
        @DisplayName("should return CONFLICT status with correct error details")
        void shouldReturnConflictStatus() {
            // given
            DeviceIdentifierAlreadyExistsException exception = new DeviceIdentifierAlreadyExistsException("DEVICE_SENSOR_001");

            // when
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleDeviceIdentifierAlreadyExists(exception);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).containsEntry("error", "DeviceIdentifierAlreadyExists");
            assertThat(response.getBody().get("message")).asString().contains("DEVICE_SENSOR_001");
        }
    }

    @Nested
    @DisplayName("handleValidationExceptions")
    class HandleValidationExceptions {

        @Test
        @DisplayName("should return BAD_REQUEST status with field errors")
        void shouldReturnBadRequestWithFieldErrors() {
            // given
            MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);

            FieldError fieldError1 = new FieldError("object", "username", "must not be blank");
            FieldError fieldError2 = new FieldError("object", "email", "must be a valid email");

            when(exception.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

            // when
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationExceptions(exception);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).containsEntry("error", "ValidationFailed");
            assertThat(response.getBody()).containsEntry("message", "Request validation failed");

            @SuppressWarnings("unchecked")
            Map<String, String> errors = (Map<String, String>) response.getBody().get("errors");
            assertThat(errors).containsEntry("username", "must not be blank");
            assertThat(errors).containsEntry("email", "must be a valid email");
        }

        @Test
        @DisplayName("should handle empty validation errors")
        void shouldHandleEmptyValidationErrors() {
            // given
            MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);

            when(exception.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getAllErrors()).thenReturn(List.of());

            // when
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationExceptions(exception);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).containsEntry("error", "ValidationFailed");

            @SuppressWarnings("unchecked")
            Map<String, String> errors = (Map<String, String>) response.getBody().get("errors");
            assertThat(errors).isEmpty();
        }
    }
}
