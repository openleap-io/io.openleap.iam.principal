package io.openleap.iam.principal.service;

import io.openleap.iam.principal.domain.dto.CreateSystemPrincipalCommand;
import io.openleap.iam.principal.domain.dto.SystemPrincipalCreated;
import io.openleap.iam.principal.domain.entity.*;
import io.openleap.iam.principal.domain.event.SystemPrincipalCreatedEvent;
import io.openleap.iam.principal.domain.mapper.SystemPrincipalMapper;
import io.openleap.iam.principal.exception.SystemIdentifierAlreadyExistsException;
import io.openleap.iam.principal.exception.TenantNotFoundException;
import io.openleap.iam.principal.exception.UsernameAlreadyExistsException;
import io.openleap.iam.principal.repository.SystemPrincipalRepository;
import io.openleap.common.messaging.event.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("SystemPrincipalService Unit Tests")
@ExtendWith(MockitoExtension.class)
class SystemPrincipalServiceTest {

    @Mock
    private SystemPrincipalRepository systemPrincipalRepository;

    @Mock
    private TenantService tenantService;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private SystemPrincipalMapper systemPrincipalMapper;

    private SystemPrincipalService systemPrincipalService;

    @BeforeEach
    void setUp() {
        systemPrincipalService = new SystemPrincipalService(
                systemPrincipalRepository,
                tenantService,
                eventPublisher,
                systemPrincipalMapper
        );
    }

    @Nested
    @DisplayName("createSystemPrincipal")
    class CreateSystemPrincipal {

        @Test
        @DisplayName("should create system principal successfully")
        void shouldCreateSystemPrincipalSuccessfully() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateSystemPrincipalCommand command = new CreateSystemPrincipalCommand(
                    "ERP_SYSTEM_001",
                    IntegrationType.ERP,
                    tenantId,
                    "SHA256:abc123def456",
                    Map.of("erp", "integration"),
                    List.of("inventory.read", "orders.write")
            );

            when(systemPrincipalRepository.existsBySystemIdentifier("ERP_SYSTEM_001")).thenReturn(false);
            when(systemPrincipalRepository.existsByUsername("erp_system_001")).thenReturn(false);
            when(tenantService.tenantExists(tenantId)).thenReturn(true);

            SystemPrincipalEntity savedEntity = createSystemPrincipalEntity("erp_system_001", "ERP_SYSTEM_001");
            when(systemPrincipalRepository.save(any(SystemPrincipalEntity.class))).thenReturn(savedEntity);

            when(systemPrincipalMapper.toSystemPrincipalCreatedEvent(any())).thenReturn(mock(SystemPrincipalCreatedEvent.class));

            SystemPrincipalCreated expectedResult = new SystemPrincipalCreated(
                    savedEntity.getBusinessId().value(), "erp_system_001", "ERP_SYSTEM_001",
                    "ERP", "SHA256:abc123def456", List.of("inventory.read", "orders.write")
            );
            when(systemPrincipalMapper.toSystemPrincipalCreated(any())).thenReturn(expectedResult);

            // when
            SystemPrincipalCreated result = systemPrincipalService.createSystemPrincipal(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(savedEntity.getBusinessId().value());
            assertThat(result.username()).isEqualTo("erp_system_001");
            assertThat(result.systemIdentifier()).isEqualTo("ERP_SYSTEM_001");
            assertThat(result.certificateThumbprint()).isEqualTo("SHA256:abc123def456");
            verify(systemPrincipalRepository).save(any(SystemPrincipalEntity.class));
            verify(systemPrincipalMapper).toSystemPrincipalCreatedEvent(any());
            verify(systemPrincipalMapper).toSystemPrincipalCreated(any());
        }

        @Test
        @DisplayName("should throw SystemIdentifierAlreadyExistsException when identifier exists")
        void shouldThrowExceptionWhenIdentifierExists() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateSystemPrincipalCommand command = new CreateSystemPrincipalCommand(
                    "EXISTING_SYSTEM",
                    IntegrationType.ERP,
                    tenantId,
                    "SHA256:abc123",
                    null,
                    null
            );

            when(systemPrincipalRepository.existsBySystemIdentifier("EXISTING_SYSTEM")).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> systemPrincipalService.createSystemPrincipal(command))
                    .isInstanceOf(SystemIdentifierAlreadyExistsException.class)
                    .hasMessageContaining("EXISTING_SYSTEM");

            verify(systemPrincipalRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw UsernameAlreadyExistsException when derived username exists")
        void shouldThrowExceptionWhenUsernameExists() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateSystemPrincipalCommand command = new CreateSystemPrincipalCommand(
                    "NEW_SYSTEM",
                    IntegrationType.ERP,
                    tenantId,
                    "SHA256:abc123",
                    null,
                    null
            );

            when(systemPrincipalRepository.existsBySystemIdentifier("NEW_SYSTEM")).thenReturn(false);
            when(systemPrincipalRepository.existsByUsername("new_system")).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> systemPrincipalService.createSystemPrincipal(command))
                    .isInstanceOf(UsernameAlreadyExistsException.class)
                    .hasMessageContaining("new_system");

            verify(systemPrincipalRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw TenantNotFoundException when tenant does not exist")
        void shouldThrowExceptionWhenTenantNotFound() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateSystemPrincipalCommand command = new CreateSystemPrincipalCommand(
                    "NEW_SYSTEM",
                    IntegrationType.ERP,
                    tenantId,
                    "SHA256:abc123",
                    null,
                    null
            );

            when(systemPrincipalRepository.existsBySystemIdentifier("NEW_SYSTEM")).thenReturn(false);
            when(systemPrincipalRepository.existsByUsername("new_system")).thenReturn(false);
            when(tenantService.tenantExists(tenantId)).thenReturn(false);

            // when / then
            assertThatThrownBy(() -> systemPrincipalService.createSystemPrincipal(command))
                    .isInstanceOf(TenantNotFoundException.class);

            verify(systemPrincipalRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when certificate thumbprint is missing")
        void shouldThrowExceptionWhenCertificateThumbprintMissing() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateSystemPrincipalCommand command = new CreateSystemPrincipalCommand(
                    "NEW_SYSTEM",
                    IntegrationType.ERP,
                    tenantId,
                    null, // missing certificate thumbprint
                    null,
                    null
            );

            when(systemPrincipalRepository.existsBySystemIdentifier("NEW_SYSTEM")).thenReturn(false);
            when(systemPrincipalRepository.existsByUsername("new_system")).thenReturn(false);
            when(tenantService.tenantExists(tenantId)).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> systemPrincipalService.createSystemPrincipal(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Certificate thumbprint is required");

            verify(systemPrincipalRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when certificate thumbprint is blank")
        void shouldThrowExceptionWhenCertificateThumbprintBlank() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateSystemPrincipalCommand command = new CreateSystemPrincipalCommand(
                    "NEW_SYSTEM",
                    IntegrationType.ERP,
                    tenantId,
                    "   ", // blank certificate thumbprint
                    null,
                    null
            );

            when(systemPrincipalRepository.existsBySystemIdentifier("NEW_SYSTEM")).thenReturn(false);
            when(systemPrincipalRepository.existsByUsername("new_system")).thenReturn(false);
            when(tenantService.tenantExists(tenantId)).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> systemPrincipalService.createSystemPrincipal(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Certificate thumbprint is required");
        }

        @Test
        @DisplayName("should set initial status to ACTIVE")
        void shouldSetInitialStatusToActive() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateSystemPrincipalCommand command = new CreateSystemPrincipalCommand(
                    "NEW_SYSTEM",
                    IntegrationType.ERP,
                    tenantId,
                    "SHA256:abc123",
                    null,
                    null
            );

            when(systemPrincipalRepository.existsBySystemIdentifier("NEW_SYSTEM")).thenReturn(false);
            when(systemPrincipalRepository.existsByUsername("new_system")).thenReturn(false);
            when(tenantService.tenantExists(tenantId)).thenReturn(true);
            when(systemPrincipalMapper.toSystemPrincipalCreatedEvent(any())).thenReturn(mock(SystemPrincipalCreatedEvent.class));
            when(systemPrincipalMapper.toSystemPrincipalCreated(any())).thenReturn(mock(SystemPrincipalCreated.class));

            ArgumentCaptor<SystemPrincipalEntity> captor = ArgumentCaptor.forClass(SystemPrincipalEntity.class);
            when(systemPrincipalRepository.save(captor.capture())).thenAnswer(inv -> {
                SystemPrincipalEntity entity = inv.getArgument(0);
                entity.setBusinessId(PrincipalId.of(UUID.randomUUID()));
                return entity;
            });

            // when
            systemPrincipalService.createSystemPrincipal(command);

            // then
            SystemPrincipalEntity savedEntity = captor.getValue();
            assertThat(savedEntity.getStatus()).isEqualTo(PrincipalStatus.ACTIVE);
        }

        @Test
        @DisplayName("should set sync status to SYNCED (no Keycloak sync needed)")
        void shouldSetSyncStatusToSynced() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateSystemPrincipalCommand command = new CreateSystemPrincipalCommand(
                    "NEW_SYSTEM",
                    IntegrationType.ERP,
                    tenantId,
                    "SHA256:abc123",
                    null,
                    null
            );

            when(systemPrincipalRepository.existsBySystemIdentifier("NEW_SYSTEM")).thenReturn(false);
            when(systemPrincipalRepository.existsByUsername("new_system")).thenReturn(false);
            when(tenantService.tenantExists(tenantId)).thenReturn(true);
            when(systemPrincipalMapper.toSystemPrincipalCreatedEvent(any())).thenReturn(mock(SystemPrincipalCreatedEvent.class));
            when(systemPrincipalMapper.toSystemPrincipalCreated(any())).thenReturn(mock(SystemPrincipalCreated.class));

            ArgumentCaptor<SystemPrincipalEntity> captor = ArgumentCaptor.forClass(SystemPrincipalEntity.class);
            when(systemPrincipalRepository.save(captor.capture())).thenAnswer(inv -> {
                SystemPrincipalEntity entity = inv.getArgument(0);
                entity.setBusinessId(PrincipalId.of(UUID.randomUUID()));
                return entity;
            });

            // when
            systemPrincipalService.createSystemPrincipal(command);

            // then
            SystemPrincipalEntity savedEntity = captor.getValue();
            assertThat(savedEntity.getSyncStatus()).isEqualTo(SyncStatus.SYNCED);
        }

        @Test
        @DisplayName("should convert system identifier to lowercase for username")
        void shouldConvertIdentifierToLowercaseForUsername() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateSystemPrincipalCommand command = new CreateSystemPrincipalCommand(
                    "MY_SYSTEM_ID",
                    IntegrationType.ERP,
                    tenantId,
                    "SHA256:abc123",
                    null,
                    null
            );

            when(systemPrincipalRepository.existsBySystemIdentifier("MY_SYSTEM_ID")).thenReturn(false);
            when(systemPrincipalRepository.existsByUsername("my_system_id")).thenReturn(false);
            when(tenantService.tenantExists(tenantId)).thenReturn(true);
            when(systemPrincipalMapper.toSystemPrincipalCreatedEvent(any())).thenReturn(mock(SystemPrincipalCreatedEvent.class));
            when(systemPrincipalMapper.toSystemPrincipalCreated(any())).thenReturn(mock(SystemPrincipalCreated.class));

            ArgumentCaptor<SystemPrincipalEntity> captor = ArgumentCaptor.forClass(SystemPrincipalEntity.class);
            when(systemPrincipalRepository.save(captor.capture())).thenAnswer(inv -> {
                SystemPrincipalEntity entity = inv.getArgument(0);
                entity.setBusinessId(PrincipalId.of(UUID.randomUUID()));
                return entity;
            });

            // when
            systemPrincipalService.createSystemPrincipal(command);

            // then
            SystemPrincipalEntity savedEntity = captor.getValue();
            assertThat(savedEntity.getUsername()).isEqualTo("my_system_id");
        }

//        @Test
//        @DisplayName("should handle null integration type")
//        void shouldHandleNullIntegrationType() {
//            // given
//            UUID tenantId = UUID.randomUUID();
//            CreateSystemPrincipalCommand command = new CreateSystemPrincipalCommand(
//                    "NEW_SYSTEM",
//                    null, // null integration type
//                    tenantId,
//                    "SHA256:abc123",
//                    null,
//                    null
//            );
//
//            when(systemPrincipalRepository.existsBySystemIdentifier("NEW_SYSTEM")).thenReturn(false);
//            when(systemPrincipalRepository.existsByUsername("new_system")).thenReturn(false);
//
//            ArgumentCaptor<SystemPrincipalEntity> captor = ArgumentCaptor.forClass(SystemPrincipalEntity.class);
//            when(systemPrincipalRepository.save(captor.capture())).thenAnswer(inv -> {
//                SystemPrincipalEntity entity = inv.getArgument(0);
//                return entity;
//            });
//
//            // when
//            SystemPrincipalCreated result = systemPrincipalService.createSystemPrincipal(command);
//
//            // then
//            assertThat(result.integrationType()).isNull();
//        }
    }

    // Helper methods

    private SystemPrincipalEntity createSystemPrincipalEntity(String username, String systemIdentifier) {
        SystemPrincipalEntity entity = new SystemPrincipalEntity();
        entity.setBusinessId(PrincipalId.of(UUID.randomUUID()));
        entity.setUsername(username);
        entity.setSystemIdentifier(systemIdentifier);
        entity.setIntegrationType(IntegrationType.ERP);
        entity.setCertificateThumbprint("SHA256:abc123def456");
        entity.setStatus(PrincipalStatus.ACTIVE);
        entity.setSyncStatus(SyncStatus.SYNCED);
        return entity;
    }
}
