package io.openleap.iam.principal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.openleap.iam.principal.controller.dto.*;
import io.openleap.iam.principal.controller.exception.PrincipalExceptionHandler;
import io.openleap.iam.principal.controller.mapper.PrincipalMapper;
import io.openleap.iam.principal.domain.dto.*;
import io.openleap.iam.principal.exception.ServiceNameAlreadyExistsException;
import io.openleap.iam.principal.exception.TenantNotFoundException;
import io.openleap.iam.principal.service.ServicePrincipalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServicePrincipalController.class)
@Import(PrincipalExceptionHandler.class)
@DisplayName("ServicePrincipalController Integration Tests")
class ServicePrincipalControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ServicePrincipalService servicePrincipalService;

    @MockBean
    private PrincipalMapper principalMapper;

    private static final String BASE_URL = "/api/v1/iam/principals";

    @Nested
    @DisplayName("POST /api/v1/iam/principals/service - Create Service Principal")
    class CreateServicePrincipal {

        @Test
        @WithMockUser
        @DisplayName("should create service principal successfully")
        void shouldCreateServicePrincipalSuccessfully() throws Exception {
            // given
            UUID principalId = UUID.randomUUID();
            UUID tenantId = UUID.randomUUID();

            CreateServicePrincipalRequestDto request = new CreateServicePrincipalRequestDto();
            request.setServiceName("PaymentService");
            request.setDefaultTenantId(tenantId);
            request.setAllowedScopes(List.of("payments.read", "payments.write"));

            CreateServicePrincipalCommand command = new CreateServicePrincipalCommand(
                    "PaymentService", tenantId, null, List.of("payments.read", "payments.write")
            );

            ServicePrincipalCreated created = new ServicePrincipalCreated(
                    principalId, "paymentservice", "PaymentService",
                    "sk_live_testApiKey", "keycloak-client-id", "keycloak-client-secret",
                    List.of("payments.read", "payments.write"), LocalDate.now().plusDays(90)
            );

            CreateServicePrincipalResponseDto responseDto = new CreateServicePrincipalResponseDto();
            responseDto.setId(principalId);
            responseDto.setUsername("paymentservice");
            responseDto.setStatus("ACTIVE");

            when(principalMapper.toCommand(any(CreateServicePrincipalRequestDto.class))).thenReturn(command);
            when(servicePrincipalService.createServicePrincipal(command)).thenReturn(created);
            when(principalMapper.toResponseDto(created)).thenReturn(responseDto);

            // when / then
            mockMvc.perform(post(BASE_URL + "/service")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.principalId").value(principalId.toString()))
                    .andExpect(jsonPath("$.username").value("paymentservice"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return CONFLICT when service name already exists")
        void shouldReturnConflictWhenServiceNameExists() throws Exception {
            // given
            CreateServicePrincipalRequestDto request = new CreateServicePrincipalRequestDto();
            request.setServiceName("ExistingService");
            request.setDefaultTenantId(UUID.randomUUID());
            request.setAllowedScopes(List.of("read"));

            when(principalMapper.toCommand(any(CreateServicePrincipalRequestDto.class))).thenReturn(mock(CreateServicePrincipalCommand.class));
            when(servicePrincipalService.createServicePrincipal(any(CreateServicePrincipalCommand.class)))
                    .thenThrow(new ServiceNameAlreadyExistsException("ExistingService"));

            // when / then
            mockMvc.perform(post(BASE_URL + "/service")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("ServiceNameAlreadyExistsException"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return NOT_FOUND when tenant does not exist")
        void shouldReturnNotFoundWhenTenantNotExists() throws Exception {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateServicePrincipalRequestDto request = new CreateServicePrincipalRequestDto();
            request.setServiceName("NewService");
            request.setDefaultTenantId(tenantId);
            request.setAllowedScopes(List.of("read"));

            when(principalMapper.toCommand(any(CreateServicePrincipalRequestDto.class))).thenReturn(mock(CreateServicePrincipalCommand.class));
            when(servicePrincipalService.createServicePrincipal(any(CreateServicePrincipalCommand.class)))
                    .thenThrow(new TenantNotFoundException(tenantId));

            // when / then
            mockMvc.perform(post(BASE_URL + "/service")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("TenantNotFound"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/iam/principals/{id}/rotate-credentials - Rotate Credentials")
    class RotateCredentials {

        @Test
        @WithMockUser
        @DisplayName("should rotate credentials successfully")
        void shouldRotateCredentialsSuccessfully() throws Exception {
            // given
            UUID principalId = UUID.randomUUID();

            RotateCredentialsRequestDto request = new RotateCredentialsRequestDto();
            request.setForce(true);
            request.setReason("Security incident");

            RotateCredentialsCommand command = new RotateCredentialsCommand(
                    principalId, true, "Security incident"
            );

            CredentialsRotated rotated = new CredentialsRotated(
                    principalId, "sk_live_newApiKey", "new-keycloak-secret",
                    LocalDate.now().plusDays(90), Instant.now()
            );

            RotateCredentialsResponseDto responseDto = new RotateCredentialsResponseDto();
            responseDto.setId(principalId.toString());
            responseDto.setApiKey("sk_live_newApiKey");
            responseDto.setKeycloakClientSecret("new-keycloak-secret");

            when(principalMapper.toCommand(any(RotateCredentialsRequestDto.class), eq(principalId))).thenReturn(command);
            when(servicePrincipalService.rotateCredentials(command)).thenReturn(rotated);
            when(principalMapper.toResponseDto(rotated)).thenReturn(responseDto);

            // when / then
            mockMvc.perform(post(BASE_URL + "/{principalId}/rotate-credentials", principalId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.principalId").value(principalId.toString()))
                    .andExpect(jsonPath("$.apiKey").value("sk_live_newApiKey"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return error when rotation not due and not forced")
        void shouldReturnErrorWhenRotationNotDue() throws Exception {
            // given
            UUID principalId = UUID.randomUUID();

            RotateCredentialsRequestDto request = new RotateCredentialsRequestDto();
            request.setForce(false);

            when(principalMapper.toCommand(any(RotateCredentialsRequestDto.class), eq(principalId)))
                    .thenReturn(new RotateCredentialsCommand(principalId, false, null));
            when(servicePrincipalService.rotateCredentials(any()))
                    .thenThrow(new IllegalStateException("Credential rotation is not due"));

            // when / then
            mockMvc.perform(post(BASE_URL + "/{principalId}/rotate-credentials", principalId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError());
        }
    }
}
