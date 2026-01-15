package io.openleap.iam.principal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.openleap.iam.principal.controller.dto.*;
import io.openleap.iam.principal.controller.exception.PrincipalExceptionHandler;
import io.openleap.iam.principal.controller.mapper.PrincipalMapper;
import io.openleap.iam.principal.domain.dto.*;
import io.openleap.iam.principal.domain.entity.DeviceType;
import io.openleap.iam.principal.exception.DeviceIdentifierAlreadyExistsException;
import io.openleap.iam.principal.exception.TenantNotFoundException;
import io.openleap.iam.principal.service.DevicePrincipalService;
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

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DevicePrincipalController.class)
@Import(PrincipalExceptionHandler.class)
@DisplayName("DevicePrincipalController Integration Tests")
class DevicePrincipalControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DevicePrincipalService devicePrincipalService;

    @MockBean
    private PrincipalMapper principalMapper;

    private static final String BASE_URL = "/api/v1/iam/principals";

    @Nested
    @DisplayName("POST /api/v1/iam/principals/device - Create Device Principal")
    class CreateDevicePrincipal {

        @Test
        @WithMockUser
        @DisplayName("should create device principal successfully")
        void shouldCreateDevicePrincipalSuccessfully() throws Exception {
            // given
            UUID principalId = UUID.randomUUID();
            UUID tenantId = UUID.randomUUID();

            CreateDevicePrincipalRequestDto request = new CreateDevicePrincipalRequestDto();
            request.setDeviceIdentifier("DEVICE_SENSOR_001");
            request.setDeviceType(DeviceType.IOT_SENSOR);
            request.setPrimaryTenantId(tenantId);
            request.setCertificateThumbprint("SHA256:device123");
            request.setManufacturer("Acme Corp");
            request.setModel("SensorX200");
            request.setFirmwareVersion("v1.2.3");
            request.setLocationInfo(Map.of("building", "A", "floor", "3"));

            CreateDevicePrincipalCommand command = new CreateDevicePrincipalCommand(
                    "DEVICE_SENSOR_001", DeviceType.IOT_SENSOR, tenantId,
                    "Acme Corp", "SensorX200", "v1.2.3",
                    "SHA256:device123", Map.of("building", "A", "floor", "3"), null
            );

            DevicePrincipalCreated created = new DevicePrincipalCreated(
                    principalId, "device_sensor_001", "DEVICE_SENSOR_001",
                    DeviceType.IOT_SENSOR, "Acme Corp", "SensorX200", "SHA256:device123"
            );

            CreateDevicePrincipalResponseDto responseDto = new CreateDevicePrincipalResponseDto();
            responseDto.setPrincipalId(principalId);
            responseDto.setUsername("device_sensor_001");
            responseDto.setStatus("ACTIVE");

            when(principalMapper.toCommand(any(CreateDevicePrincipalRequestDto.class))).thenReturn(command);
            when(devicePrincipalService.createDevicePrincipal(command)).thenReturn(created);
            when(principalMapper.toResponseDto(created)).thenReturn(responseDto);

            // when / then
            mockMvc.perform(post(BASE_URL + "/device")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.principalId").value(principalId.toString()))
                    .andExpect(jsonPath("$.username").value("device_sensor_001"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return CONFLICT when device identifier already exists")
        void shouldReturnConflictWhenIdentifierExists() throws Exception {
            // given
            CreateDevicePrincipalRequestDto request = new CreateDevicePrincipalRequestDto();
            request.setDeviceIdentifier("EXISTING_DEVICE");
            request.setDeviceType(DeviceType.IOT_SENSOR);
            request.setPrimaryTenantId(UUID.randomUUID());
            request.setCertificateThumbprint("SHA256:abc123");
            request.setManufacturer("Manufacturer");
            request.setModel("Model");

            when(principalMapper.toCommand(any(CreateDevicePrincipalRequestDto.class))).thenReturn(mock(CreateDevicePrincipalCommand.class));
            when(devicePrincipalService.createDevicePrincipal(any(CreateDevicePrincipalCommand.class)))
                    .thenThrow(new DeviceIdentifierAlreadyExistsException("EXISTING_DEVICE"));

            // when / then
            mockMvc.perform(post(BASE_URL + "/device")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("DeviceIdentifierAlreadyExists"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return NOT_FOUND when tenant does not exist")
        void shouldReturnNotFoundWhenTenantNotExists() throws Exception {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateDevicePrincipalRequestDto request = new CreateDevicePrincipalRequestDto();
            request.setDeviceIdentifier("NEW_DEVICE");
            request.setDeviceType(DeviceType.IOT_SENSOR);
            request.setPrimaryTenantId(tenantId);
            request.setCertificateThumbprint("SHA256:abc123");
            request.setManufacturer("Manufacturer");
            request.setModel("Model");

            when(principalMapper.toCommand(any(CreateDevicePrincipalRequestDto.class))).thenReturn(mock(CreateDevicePrincipalCommand.class));
            when(devicePrincipalService.createDevicePrincipal(any(CreateDevicePrincipalCommand.class)))
                    .thenThrow(new TenantNotFoundException(tenantId));

            // when / then
            mockMvc.perform(post(BASE_URL + "/device")
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
            CreateDevicePrincipalRequestDto request = new CreateDevicePrincipalRequestDto();
            request.setDeviceIdentifier("NEW_DEVICE");
            request.setDeviceType(DeviceType.IOT_SENSOR);
            request.setPrimaryTenantId(UUID.randomUUID());
            request.setCertificateThumbprint(null); // missing
            request.setManufacturer("Manufacturer");
            request.setModel("Model");

            when(principalMapper.toCommand(any(CreateDevicePrincipalRequestDto.class))).thenReturn(mock(CreateDevicePrincipalCommand.class));
            when(devicePrincipalService.createDevicePrincipal(any(CreateDevicePrincipalCommand.class)))
                    .thenThrow(new IllegalArgumentException("Certificate thumbprint is required"));

            // when / then
            mockMvc.perform(post(BASE_URL + "/device")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @WithMockUser
        @DisplayName("should create device principal with optional fields null")
        void shouldCreateDeviceWithOptionalFieldsNull() throws Exception {
            // given
            UUID principalId = UUID.randomUUID();
            UUID tenantId = UUID.randomUUID();

            CreateDevicePrincipalRequestDto request = new CreateDevicePrincipalRequestDto();
            request.setDeviceIdentifier("DEVICE_001");
            request.setDeviceType(null); // optional
            request.setPrimaryTenantId(tenantId);
            request.setCertificateThumbprint("SHA256:abc123");
            request.setManufacturer("Manufacturer");
            request.setModel("Model");
            // firmwareVersion and locationInfo left null

            CreateDevicePrincipalCommand command = new CreateDevicePrincipalCommand(
                    "DEVICE_001", null, tenantId,
                    "SHA256:abc123", "Manufacturer", "Model",
                    null, null, null
            );

            DevicePrincipalCreated created = new DevicePrincipalCreated(
                    principalId, "device_001", "DEVICE_001",
                    null, "Manufacturer", "Model", "SHA256:abc123"
            );

            CreateDevicePrincipalResponseDto responseDto = new CreateDevicePrincipalResponseDto();
            responseDto.setPrincipalId(principalId);
            responseDto.setUsername("device_001");
            responseDto.setStatus("ACTIVE");

            when(principalMapper.toCommand(any(CreateDevicePrincipalRequestDto.class))).thenReturn(command);
            when(devicePrincipalService.createDevicePrincipal(command)).thenReturn(created);
            when(principalMapper.toResponseDto(created)).thenReturn(responseDto);

            // when / then
            mockMvc.perform(post(BASE_URL + "/device")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.principalId").value(principalId.toString()));
        }
    }
}
