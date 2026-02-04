package io.openleap.iam.principal.service;

import io.openleap.iam.principal.domain.dto.CreateDevicePrincipalCommand;
import io.openleap.iam.principal.domain.dto.DevicePrincipalCreated;
import io.openleap.iam.principal.domain.entity.*;
import io.openleap.iam.principal.domain.mapper.DevicePrincipalMapper;
import io.openleap.iam.principal.exception.DeviceIdentifierAlreadyExistsException;
import io.openleap.iam.principal.exception.TenantNotFoundException;
import io.openleap.iam.principal.exception.UsernameAlreadyExistsException;
import io.openleap.iam.principal.repository.DevicePrincipalRepository;
import io.openleap.iam.principal.repository.PrincipalTenantMembershipRepository;
import io.openleap.common.messaging.event.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("DevicePrincipalService Unit Tests")
@ExtendWith(MockitoExtension.class)
class DevicePrincipalServiceTest {

    @Mock
    private DevicePrincipalRepository devicePrincipalRepository;

    @Mock
    private TenantService tenantService;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private DevicePrincipalMapper devicePrincipalMapper;

    private DevicePrincipalService devicePrincipalService;

    @BeforeEach
    void setUp() {
        devicePrincipalService = new DevicePrincipalService(
                devicePrincipalRepository,
                tenantService,
                eventPublisher,
                devicePrincipalMapper
        );
    }

    @Nested
    @DisplayName("createDevicePrincipal")
    class CreateDevicePrincipal {

        @Test
        @DisplayName("should create device principal successfully")
        void shouldCreateDevicePrincipalSuccessfully() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateDevicePrincipalCommand command = new CreateDevicePrincipalCommand(
                    "DEVICE_SENSOR_001",
                    DeviceType.IOT_SENSOR,
                    tenantId,
                    "Acme Corp",
                    "SensorX200",
                    "v1.2.3",
                    "SHA256:device123",
                    Map.of("building", "A", "floor", "3"),
                    Map.of("sensor", "iot")
            );

            when(devicePrincipalRepository.existsByDeviceIdentifier("DEVICE_SENSOR_001")).thenReturn(false);
            when(devicePrincipalRepository.existsByUsername("device_sensor_001")).thenReturn(false);
//            when(tenantService.tenantExists(tenantId)).thenReturn(true);

            DevicePrincipalEntity savedEntity = createDevicePrincipalEntity("device_sensor_001", "DEVICE_SENSOR_001");
            when(devicePrincipalRepository.save(any(DevicePrincipalEntity.class))).thenReturn(savedEntity);

            DevicePrincipalCreated expectedCreated = new DevicePrincipalCreated(
                    savedEntity.getBusinessId().value(),
                    "device_sensor_001",
                    "DEVICE_SENSOR_001",
                    DeviceType.IOT_SENSOR,
                    "Acme Corp",
                    "SensorX200",
                    "SHA256:device123"
            );
            when(devicePrincipalMapper.toDevicePrincipalCreated(any(DevicePrincipalEntity.class))).thenReturn(expectedCreated);

            // when
            DevicePrincipalCreated result = devicePrincipalService.createDevicePrincipal(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.id().toString()).isEqualTo(savedEntity.getBusinessId().toString());
            assertThat(result.username()).isEqualTo("device_sensor_001");
            assertThat(result.deviceIdentifier()).isEqualTo("DEVICE_SENSOR_001");
            assertThat(result.deviceType()).isEqualTo(DeviceType.IOT_SENSOR);
            assertThat(result.manufacturer()).isEqualTo("Acme Corp");
            assertThat(result.model()).isEqualTo("SensorX200");
            verify(devicePrincipalRepository).save(any(DevicePrincipalEntity.class));
//            verify(membershipRepository).save(any());
        }

        @Test
        @DisplayName("should throw DeviceIdentifierAlreadyExistsException when identifier exists")
        void shouldThrowExceptionWhenIdentifierExists() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateDevicePrincipalCommand command = new CreateDevicePrincipalCommand(
                    "EXISTING_DEVICE",
                    DeviceType.IOT_SENSOR,
                    tenantId,
                    "Manufacturer",
                    "Model",
                    "v1.0",
                    "SHA256:abc123",
                    null,
                    null
            );

            when(devicePrincipalRepository.existsByDeviceIdentifier("EXISTING_DEVICE")).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> devicePrincipalService.createDevicePrincipal(command))
                    .isInstanceOf(DeviceIdentifierAlreadyExistsException.class)
                    .hasMessageContaining("EXISTING_DEVICE");

            verify(devicePrincipalRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw UsernameAlreadyExistsException when derived username exists")
        void shouldThrowExceptionWhenUsernameExists() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateDevicePrincipalCommand command = new CreateDevicePrincipalCommand(
                    "NEW_DEVICE",
                    DeviceType.IOT_SENSOR,
                    tenantId,
                    "Manufacturer",
                    "Model",
                    "v1.0",
                    "SHA256:abc123",
                    null,
                    null
            );

            when(devicePrincipalRepository.existsByDeviceIdentifier("NEW_DEVICE")).thenReturn(false);
            when(devicePrincipalRepository.existsByUsername("new_device")).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> devicePrincipalService.createDevicePrincipal(command))
                    .isInstanceOf(UsernameAlreadyExistsException.class)
                    .hasMessageContaining("new_device");

            verify(devicePrincipalRepository, never()).save(any());
        }

//        @Test
//        @DisplayName("should throw TenantNotFoundException when tenant does not exist")
//        void shouldThrowExceptionWhenTenantNotFound() {
//            // given
//            UUID tenantId = UUID.randomUUID();
//            CreateDevicePrincipalCommand command = new CreateDevicePrincipalCommand(
//                    "NEW_DEVICE",
//                    DeviceType.IOT_SENSOR,
//                    tenantId,
//                    "Manufacturer",
//                    "Model",
//                    "v1.0",
//                    "SHA256:abc123",
//                    null,
//                    null
//            );
//
//            when(devicePrincipalRepository.existsByDeviceIdentifier("NEW_DEVICE")).thenReturn(false);
//            when(devicePrincipalRepository.existsByUsername("new_device")).thenReturn(false);
//            when(tenantService.tenantExists(tenantId)).thenReturn(false);
//
//            // when / then
//            assertThatThrownBy(() -> devicePrincipalService.createDevicePrincipal(command))
//                    .isInstanceOf(TenantNotFoundException.class);
//
//            verify(devicePrincipalRepository, never()).save(any());
//        }

        @Test
        @DisplayName("should throw IllegalArgumentException when certificate thumbprint is missing")
        void shouldThrowExceptionWhenCertificateThumbprintMissing() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateDevicePrincipalCommand command = new CreateDevicePrincipalCommand(
                    "NEW_DEVICE",
                    DeviceType.IOT_SENSOR,
                    tenantId,
                    "Manufacturer",
                    "Model",
                    "v1.0",
                    null, // missing certificate thumbprint
                    null,
                    null
            );

            when(devicePrincipalRepository.existsByDeviceIdentifier("NEW_DEVICE")).thenReturn(false);
            when(devicePrincipalRepository.existsByUsername("new_device")).thenReturn(false);
//            when(tenantService.tenantExists(tenantId)).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> devicePrincipalService.createDevicePrincipal(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Certificate thumbprint is required");

            verify(devicePrincipalRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when certificate thumbprint is blank")
        void shouldThrowExceptionWhenCertificateThumbprintBlank() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateDevicePrincipalCommand command = new CreateDevicePrincipalCommand(
                    "NEW_DEVICE",
                    DeviceType.IOT_SENSOR,
                    tenantId,
                    "Manufacturer",
                    "Model",
                    "v1.0",
                    "   ", // blank certificate thumbprint
                    null,
                    null
            );

            when(devicePrincipalRepository.existsByDeviceIdentifier("NEW_DEVICE")).thenReturn(false);
            when(devicePrincipalRepository.existsByUsername("new_device")).thenReturn(false);
//            when(tenantService.tenantExists(tenantId)).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> devicePrincipalService.createDevicePrincipal(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Certificate thumbprint is required");
        }

        @Test
        @DisplayName("should set initial status to ACTIVE")
        void shouldSetInitialStatusToActive() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateDevicePrincipalCommand command = new CreateDevicePrincipalCommand(
                    "NEW_DEVICE",
                    DeviceType.IOT_SENSOR,
                    tenantId,
                    "Manufacturer",
                    "Model",
                    "v1.0",
                    "SHA256:abc123",
                    null,
                    null
            );

            when(devicePrincipalRepository.existsByDeviceIdentifier("NEW_DEVICE")).thenReturn(false);
            when(devicePrincipalRepository.existsByUsername("new_device")).thenReturn(false);
//            when(tenantService.tenantExists(tenantId)).thenReturn(true);

            ArgumentCaptor<DevicePrincipalEntity> captor = ArgumentCaptor.forClass(DevicePrincipalEntity.class);
            when(devicePrincipalRepository.save(captor.capture())).thenAnswer(inv -> {
                DevicePrincipalEntity entity = inv.getArgument(0);
                entity.setBusinessId(PrincipalId.of(UUID.randomUUID()));
                return entity;
            });

            // when
            devicePrincipalService.createDevicePrincipal(command);

            // then
            DevicePrincipalEntity savedEntity = captor.getValue();
            assertThat(savedEntity.getStatus()).isEqualTo(PrincipalStatus.ACTIVE);
        }

        @Test
        @DisplayName("should set sync status to SYNCED (no Keycloak sync needed)")
        void shouldSetSyncStatusToSynced() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateDevicePrincipalCommand command = new CreateDevicePrincipalCommand(
                    "NEW_DEVICE",
                    DeviceType.IOT_SENSOR,
                    tenantId,
                    "Manufacturer",
                    "Model",
                    "v1.0",
                    "SHA256:abc123",
                    null,
                    null
            );

            when(devicePrincipalRepository.existsByDeviceIdentifier("NEW_DEVICE")).thenReturn(false);
            when(devicePrincipalRepository.existsByUsername("new_device")).thenReturn(false);
//            when(tenantService.tenantExists(tenantId)).thenReturn(true);

            ArgumentCaptor<DevicePrincipalEntity> captor = ArgumentCaptor.forClass(DevicePrincipalEntity.class);
            when(devicePrincipalRepository.save(captor.capture())).thenAnswer(inv -> {
                DevicePrincipalEntity entity = inv.getArgument(0);
                entity.setBusinessId(PrincipalId.of(UUID.randomUUID()));
                return entity;
            });

            // when
            devicePrincipalService.createDevicePrincipal(command);

            // then
            DevicePrincipalEntity savedEntity = captor.getValue();
            assertThat(savedEntity.getSyncStatus()).isEqualTo(SyncStatus.SYNCED);
        }

        @Test
        @DisplayName("should convert device identifier to lowercase for username")
        void shouldConvertIdentifierToLowercaseForUsername() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateDevicePrincipalCommand command = new CreateDevicePrincipalCommand(
                    "MY_DEVICE_ID",
                    DeviceType.IOT_SENSOR,
                    tenantId,
                    "Manufacturer",
                    "Model",
                    "v1.0",
                    "SHA256:abc123",
                    null,
                    null
            );

            when(devicePrincipalRepository.existsByDeviceIdentifier("MY_DEVICE_ID")).thenReturn(false);
            when(devicePrincipalRepository.existsByUsername("my_device_id")).thenReturn(false);
//            when(tenantService.tenantExists(tenantId)).thenReturn(true);

            ArgumentCaptor<DevicePrincipalEntity> captor = ArgumentCaptor.forClass(DevicePrincipalEntity.class);
            when(devicePrincipalRepository.save(captor.capture())).thenAnswer(inv -> {
                DevicePrincipalEntity entity = inv.getArgument(0);
                entity.setBusinessId(PrincipalId.of(UUID.randomUUID()));
                return entity;
            });

            // when
            devicePrincipalService.createDevicePrincipal(command);

            // then
            DevicePrincipalEntity savedEntity = captor.getValue();
            assertThat(savedEntity.getUsername()).isEqualTo("my_device_id");
        }

        @Test
        @DisplayName("should handle null device type")
        void shouldHandleNullDeviceType() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateDevicePrincipalCommand command = new CreateDevicePrincipalCommand(
                    "NEW_DEVICE",
                    null, // null device type
                    tenantId,
                    "Manufacturer",
                    "Model",
                    "v1.0",
                    "SHA256:abc123",
                    null,
                    null
            );

            when(devicePrincipalRepository.existsByDeviceIdentifier("NEW_DEVICE")).thenReturn(false);
            when(devicePrincipalRepository.existsByUsername("new_device")).thenReturn(false);
//            when(tenantService.tenantExists(tenantId)).thenReturn(true);

            DevicePrincipalEntity savedEntity = new DevicePrincipalEntity();
            savedEntity.setBusinessId(PrincipalId.of(UUID.randomUUID()));
            savedEntity.setUsername("new_device");
            savedEntity.setDeviceIdentifier("NEW_DEVICE");
            savedEntity.setDeviceType(null);
            savedEntity.setManufacturer("Manufacturer");
            savedEntity.setModel("Model");
            savedEntity.setCertificateThumbprint("SHA256:abc123");
            savedEntity.setStatus(PrincipalStatus.ACTIVE);
            savedEntity.setSyncStatus(SyncStatus.SYNCED);
            when(devicePrincipalRepository.save(any())).thenReturn(savedEntity);

            DevicePrincipalCreated expectedCreated = new DevicePrincipalCreated(
                    savedEntity.getBusinessId().value(),
                    "new_device",
                    "NEW_DEVICE",
                    null,
                    "Manufacturer",
                    "Model",
                    "SHA256:abc123"
            );
            when(devicePrincipalMapper.toDevicePrincipalCreated(any(DevicePrincipalEntity.class))).thenReturn(expectedCreated);

            // when
            DevicePrincipalCreated result = devicePrincipalService.createDevicePrincipal(command);

            // then
            assertThat(result.deviceType()).isNull();
        }

        @Test
        @DisplayName("should store firmware version and location info")
        void shouldStoreFirmwareVersionAndLocationInfo() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateDevicePrincipalCommand command = new CreateDevicePrincipalCommand(
                    "NEW_DEVICE",
                    DeviceType.GATEWAY,
                    tenantId,
                    "Manufacturer",
                    "Model",
                    "v2.1.0",
                    "SHA256:abc123",
                    Map.of("warehouse", "B", "section", "5"),
                    null
            );

            when(devicePrincipalRepository.existsByDeviceIdentifier("NEW_DEVICE")).thenReturn(false);
            when(devicePrincipalRepository.existsByUsername("new_device")).thenReturn(false);
//            when(tenantService.tenantExists(tenantId)).thenReturn(true);

            ArgumentCaptor<DevicePrincipalEntity> captor = ArgumentCaptor.forClass(DevicePrincipalEntity.class);
            when(devicePrincipalRepository.save(captor.capture())).thenAnswer(inv -> {
                DevicePrincipalEntity entity = inv.getArgument(0);
                entity.setBusinessId(PrincipalId.of(UUID.randomUUID()));
                return entity;
            });

            // when
            devicePrincipalService.createDevicePrincipal(command);

            // then
            DevicePrincipalEntity savedEntity = captor.getValue();
            assertThat(savedEntity.getFirmwareVersion()).isEqualTo("v2.1.0");
        }
    }

    // Helper methods

    private DevicePrincipalEntity createDevicePrincipalEntity(String username, String deviceIdentifier) {
        DevicePrincipalEntity entity = new DevicePrincipalEntity();
        entity.setBusinessId(PrincipalId.of(UUID.randomUUID()));
        entity.setUsername(username);
        entity.setDeviceIdentifier(deviceIdentifier);
        entity.setDeviceType(DeviceType.IOT_SENSOR);
        entity.setCertificateThumbprint("SHA256:device123");
        entity.setManufacturer("Acme Corp");
        entity.setModel("SensorX200");
        entity.setStatus(PrincipalStatus.ACTIVE);
        entity.setSyncStatus(SyncStatus.SYNCED);
        return entity;
    }
}
