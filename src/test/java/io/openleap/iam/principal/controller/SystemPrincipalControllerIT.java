package io.openleap.iam.principal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.openleap.iam.principal.controller.dto.*;
import io.openleap.iam.principal.controller.exception.PrincipalExceptionHandler;
import io.openleap.iam.principal.controller.mapper.PrincipalMapper;
import io.openleap.iam.principal.domain.dto.*;
import io.openleap.iam.principal.domain.entity.IntegrationType;
import io.openleap.iam.principal.exception.SystemIdentifierAlreadyExistsException;
import io.openleap.iam.principal.exception.TenantNotFoundException;
import io.openleap.iam.principal.service.SystemPrincipalService;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SystemPrincipalController.class)
@Import(PrincipalExceptionHandler.class)
@DisplayName("SystemPrincipalController Integration Tests")
class SystemPrincipalControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SystemPrincipalService systemPrincipalService;

    @MockBean
    private PrincipalMapper principalMapper;

    private static final String BASE_URL = "/api/v1/iam/principals";

    @Nested
    @DisplayName("POST /api/v1/iam/principals/system - Create System Principal")
    class CreateSystemPrincipal {

        @Test
        @WithMockUser
        @DisplayName("should create system principal successfully")
        void shouldCreateSystemPrincipalSuccessfully() throws Exception {
            // given
            UUID principalId = UUID.randomUUID();
            UUID tenantId = UUID.randomUUID();

            CreateSystemPrincipalRequestDto request = new CreateSystemPrincipalRequestDto();
            request.setSystemIdentifier("ERP_SYSTEM_001");
            request.setIntegrationType(IntegrationType.ERP);
            request.setDefaultTenantId(tenantId);
            request.setCertificateThumbprint("SHA256:abc123def456");
            request.setAllowedOperations(List.of("inventory.read", "orders.write"));

            CreateSystemPrincipalCommand command = new CreateSystemPrincipalCommand(
                    "ERP_SYSTEM_001", IntegrationType.ERP, tenantId,
                    "SHA256:abc123def456", null, List.of("inventory.read", "orders.write")
            );

            SystemPrincipalCreated created = new SystemPrincipalCreated(
                    principalId, "erp_system_001", "ERP_SYSTEM_001",
                    "ERP", "SHA256:abc123def456", List.of("inventory.read", "orders.write")
            );

            CreateSystemPrincipalResponseDto responseDto = new CreateSystemPrincipalResponseDto();
            responseDto.setId(principalId);
            responseDto.setUsername("erp_system_001");
            responseDto.setStatus("ACTIVE");

            when(principalMapper.toCommand(any(CreateSystemPrincipalRequestDto.class))).thenReturn(command);
            when(systemPrincipalService.createSystemPrincipal(command)).thenReturn(created);
            when(principalMapper.toResponseDto(created)).thenReturn(responseDto);

            // when / then
            mockMvc.perform(post(BASE_URL + "/system")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.principalId").value(principalId.toString()))
                    .andExpect(jsonPath("$.username").value("erp_system_001"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return CONFLICT when system identifier already exists")
        void shouldReturnConflictWhenIdentifierExists() throws Exception {
            // given
            CreateSystemPrincipalRequestDto request = new CreateSystemPrincipalRequestDto();
            request.setSystemIdentifier("EXISTING_SYSTEM");
            request.setIntegrationType(IntegrationType.ERP);
            request.setDefaultTenantId(UUID.randomUUID());
            request.setCertificateThumbprint("SHA256:abc123");

            when(principalMapper.toCommand(any(CreateSystemPrincipalRequestDto.class))).thenReturn(mock(CreateSystemPrincipalCommand.class));
            when(systemPrincipalService.createSystemPrincipal(any(CreateSystemPrincipalCommand.class)))
                    .thenThrow(new SystemIdentifierAlreadyExistsException("EXISTING_SYSTEM"));

            // when / then
            mockMvc.perform(post(BASE_URL + "/system")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("SystemIdentifierAlreadyExists"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return NOT_FOUND when tenant does not exist")
        void shouldReturnNotFoundWhenTenantNotExists() throws Exception {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateSystemPrincipalRequestDto request = new CreateSystemPrincipalRequestDto();
            request.setSystemIdentifier("NEW_SYSTEM");
            request.setIntegrationType(IntegrationType.ERP);
            request.setDefaultTenantId(tenantId);
            request.setCertificateThumbprint("SHA256:abc123");

            when(principalMapper.toCommand(any(CreateSystemPrincipalRequestDto.class))).thenReturn(mock(CreateSystemPrincipalCommand.class));
            when(systemPrincipalService.createSystemPrincipal(any(CreateSystemPrincipalCommand.class)))
                    .thenThrow(new TenantNotFoundException(tenantId));

            // when / then
            mockMvc.perform(post(BASE_URL + "/system")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("TenantNotFound"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return error when certificate thumbprint is missing")
        void shouldReturnErrorWhenCertificateMissing() throws Exception {
            // given
            CreateSystemPrincipalRequestDto request = new CreateSystemPrincipalRequestDto();
            request.setSystemIdentifier("NEW_SYSTEM");
            request.setIntegrationType(IntegrationType.ERP);
            request.setDefaultTenantId(UUID.randomUUID());
            request.setCertificateThumbprint(null); // missing

            when(principalMapper.toCommand(any(CreateSystemPrincipalRequestDto.class))).thenReturn(mock(CreateSystemPrincipalCommand.class));
            when(systemPrincipalService.createSystemPrincipal(any(CreateSystemPrincipalCommand.class)))
                    .thenThrow(new IllegalArgumentException("Certificate thumbprint is required"));

            // when / then
            mockMvc.perform(post(BASE_URL + "/system")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError());
        }
    }
}
