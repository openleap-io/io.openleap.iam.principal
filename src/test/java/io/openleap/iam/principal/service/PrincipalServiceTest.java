package io.openleap.iam.principal.service;

import io.openleap.iam.principal.domain.dto.*;
import io.openleap.iam.principal.domain.entity.*;
import io.openleap.iam.principal.domain.event.PrincipalDeactivatedEvent;
import io.openleap.iam.principal.domain.event.PrincipalSuspendedEvent;
import io.openleap.iam.principal.domain.mapper.PrincipalEventMapper;
import io.openleap.iam.principal.domain.mapper.ServicePrincipalMapper;
import io.openleap.iam.principal.domain.mapper.SystemPrincipalMapper;
import io.openleap.iam.principal.repository.*;
import io.openleap.iam.principal.service.keycloak.KeycloakService;
import io.openleap.common.messaging.event.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("PrincipalService Unit Tests")
@ExtendWith(MockitoExtension.class)
class PrincipalServiceTest {

    @Mock
    private HumanPrincipalRepository humanPrincipalRepository;

    @Mock
    private ServicePrincipalRepository servicePrincipalRepository;

    @Mock
    private SystemPrincipalRepository systemPrincipalRepository;

    @Mock
    private DevicePrincipalRepository devicePrincipalRepository;

    @Mock
    private KeycloakService keycloakService;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private ServicePrincipalMapper servicePrincipalMapper;

    @Mock
    private SystemPrincipalMapper systemPrincipalMapper;

    @Mock
    private PrincipalEventMapper principalEventMapper;

    private PrincipalService principalService;

    @BeforeEach
    void setUp() {
        principalService = new PrincipalService(
                humanPrincipalRepository,
                servicePrincipalRepository,
                systemPrincipalRepository,
                devicePrincipalRepository,
                keycloakService,
                eventPublisher,
                servicePrincipalMapper,
                systemPrincipalMapper,
                principalEventMapper
        );
    }

    @Nested
    @DisplayName("findPrincipalById")
    class FindPrincipalById {

        @Test
        @DisplayName("should find human principal by ID")
        void shouldFindHumanPrincipalById() {
            // given
            UUID principalId = UUID.randomUUID();
            HumanPrincipalEntity humanPrincipal = createHumanPrincipal(principalId);
            when(humanPrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.of(humanPrincipal));

            // when
            Optional<Principal> result = principalService.findPrincipalByBusinessId(PrincipalId.of(principalId));

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getBusinessId().toString()).isEqualTo(principalId.toString());
            assertThat(result.get().getPrincipalType()).isEqualTo(PrincipalType.HUMAN);
        }

        @Test
        @DisplayName("should find service principal by ID")
        void shouldFindServicePrincipalById() {
            // given
            UUID principalId = UUID.randomUUID();
            ServicePrincipalEntity servicePrincipal = createServicePrincipal(principalId);
            when(servicePrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.empty());
            when(servicePrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.of(servicePrincipal));

            // when
            Optional<Principal> result = principalService.findPrincipalByBusinessId(PrincipalId.of(principalId));

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getPrincipalType()).isEqualTo(PrincipalType.SERVICE);
        }

        @Test
        @DisplayName("should return empty when principal not found")
        void shouldReturnEmptyWhenNotFound() {
            // given
            UUID principalId = UUID.randomUUID();
            when(humanPrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.empty());
            when(servicePrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.empty());
            when(systemPrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.empty());
            when(devicePrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.empty());

            // when
            Optional<Principal> result = principalService.findPrincipalByBusinessId(PrincipalId.of(principalId));

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("activatePrincipal")
    class ActivatePrincipal {

        @Test
        @DisplayName("should activate PENDING principal successfully")
        void shouldActivatePendingPrincipal() {
            // given
            UUID principalId = UUID.randomUUID();
            HumanPrincipalEntity principal = createHumanPrincipal(principalId);
            principal.setStatus(PrincipalStatus.PENDING);
            principal.setKeycloakUserId("keycloak-123");

            ActivatePrincipalCommand command = new ActivatePrincipalCommand(
                    principalId, null, true, "Admin activation"
            );

            when(humanPrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.of(principal));
            when(humanPrincipalRepository.save(any())).thenReturn(principal);

            // when
            PrincipalActivated result = principalService.activatePrincipal(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(principalId);
            assertThat(principal.getStatus()).isEqualTo(PrincipalStatus.ACTIVE);
            verify(keycloakService).updateUser(eq("keycloak-123"), any());
        }

        @Test
        @DisplayName("should throw exception when principal not found")
        void shouldThrowExceptionWhenPrincipalNotFound() {
            // given
            UUID principalId = UUID.randomUUID();
            ActivatePrincipalCommand command = new ActivatePrincipalCommand(principalId, null, false, null);

            when(humanPrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.empty());
            when(servicePrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.empty());
            when(systemPrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.empty());
            when(devicePrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> principalService.activatePrincipal(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("should throw exception when principal is not PENDING")
        void shouldThrowExceptionWhenNotPending() {
            // given
            UUID principalId = UUID.randomUUID();
            HumanPrincipalEntity principal = createHumanPrincipal(principalId);
            principal.setStatus(PrincipalStatus.ACTIVE);

            ActivatePrincipalCommand command = new ActivatePrincipalCommand(principalId, null, false, null);

            when(humanPrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.of(principal));

            // when / then
            assertThatThrownBy(() -> principalService.activatePrincipal(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("PENDING");
        }

        @Test
        @DisplayName("should set emailVerified when verification token provided")
        void shouldSetEmailVerifiedWithToken() {
            // given
            UUID principalId = UUID.randomUUID();
            HumanPrincipalEntity principal = createHumanPrincipal(principalId);
            principal.setStatus(PrincipalStatus.PENDING);
            principal.setEmailVerified(false);
            principal.setKeycloakUserId("keycloak-123");

            ActivatePrincipalCommand command = new ActivatePrincipalCommand(
                    principalId, "verification-token", false, null
            );

            when(humanPrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.of(principal));
            when(humanPrincipalRepository.save(any())).thenReturn(principal);

            // when
            principalService.activatePrincipal(command);

            // then
            assertThat(principal.getEmailVerified()).isTrue();
        }
    }

    @Nested
    @DisplayName("suspendPrincipal")
    class SuspendPrincipal {

        @Test
        @DisplayName("should suspend ACTIVE principal successfully")
        void shouldSuspendActivePrincipal() {
            // given
            UUID principalId = UUID.randomUUID();
            HumanPrincipalEntity principal = createHumanPrincipal(principalId);
            principal.setStatus(PrincipalStatus.ACTIVE);
            principal.setKeycloakUserId("keycloak-123");

            SuspendPrincipalCommand command = new SuspendPrincipalCommand(
                    principalId, "Security concern", "INC-12345"
            );

            when(humanPrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.of(principal));
            when(humanPrincipalRepository.save(any())).thenReturn(principal);
            when(principalEventMapper.toPrincipalSuspendedEvent(any(), any())).thenReturn(
                    new PrincipalSuspendedEvent(principalId, "HUMAN", "SUSPENDED", "Security concern", "INC-12345")
            );

            // when
            PrincipalSuspended result = principalService.suspendPrincipal(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(principalId);
            assertThat(principal.getStatus()).isEqualTo(PrincipalStatus.SUSPENDED);
            verify(keycloakService).updateUser(eq("keycloak-123"), any());
        }

        @Test
        @DisplayName("should throw exception when principal is not ACTIVE")
        void shouldThrowExceptionWhenNotActive() {
            // given
            UUID principalId = UUID.randomUUID();
            HumanPrincipalEntity principal = createHumanPrincipal(principalId);
            principal.setStatus(PrincipalStatus.PENDING);

            SuspendPrincipalCommand command = new SuspendPrincipalCommand(principalId, "Reason", null);

            when(humanPrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.of(principal));

            // when / then
            assertThatThrownBy(() -> principalService.suspendPrincipal(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ACTIVE");
        }

//        @Test
//        @DisplayName("should suspend active memberships")
//        void shouldSuspendActiveMemberships() {
//            // given
//            UUID principalId = UUID.randomUUID();
//            HumanPrincipalEntity principal = createHumanPrincipal(principalId);
//            principal.setStatus(PrincipalStatus.ACTIVE);
//
//            PrincipalTenantMembershipEntity membership = new PrincipalTenantMembershipEntity();
//            membership.setStatus(MembershipStatus.ACTIVE);
//
//            SuspendPrincipalCommand command = new SuspendPrincipalCommand(principalId, "Reason", null);
//
//            when(humanPrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.of(principal));
//            when(humanPrincipalRepository.save(any())).thenReturn(principal);
////            when(membershipRepository.findByPrincipalId(principalId)).thenReturn(List.of(membership));
//
//            // when
//            principalService.suspendPrincipal(command);
//
//            // then
//            assertThat(membership.getStatus()).isEqualTo(MembershipStatus.SUSPENDED);
////            verify(membershipRepository).save(membership);
//        }
    }

    @Nested
    @DisplayName("deactivatePrincipal")
    class DeactivatePrincipal {

        @Test
        @DisplayName("should deactivate ACTIVE principal successfully")
        void shouldDeactivateActivePrincipal() {
            // given
            UUID principalId = UUID.randomUUID();
            HumanPrincipalEntity principal = createHumanPrincipal(principalId);
            principal.setStatus(PrincipalStatus.ACTIVE);

            DeactivatePrincipalCommand command = new DeactivatePrincipalCommand(
                    principalId, "Left company", LocalDate.now()
            );

            when(humanPrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.of(principal));
            when(humanPrincipalRepository.save(any())).thenReturn(principal);
            when(principalEventMapper.toPrincipalDeactivatedEvent(any(), any())).thenReturn(
                    new PrincipalDeactivatedEvent(principalId, "HUMAN", "INACTIVE", "Left company", LocalDate.now())
            );

            // when
            PrincipalDeactivated result = principalService.deactivatePrincipal(command);

            // then
            assertThat(result).isNotNull();
            assertThat(principal.getStatus()).isEqualTo(PrincipalStatus.INACTIVE);
        }

        @Test
        @DisplayName("should deactivate SUSPENDED principal successfully")
        void shouldDeactivateSuspendedPrincipal() {
            // given
            UUID principalId = UUID.randomUUID();
            HumanPrincipalEntity principal = createHumanPrincipal(principalId);
            principal.setStatus(PrincipalStatus.SUSPENDED);

            DeactivatePrincipalCommand command = new DeactivatePrincipalCommand(
                    principalId, "Account terminated", LocalDate.now()
            );

            when(humanPrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.of(principal));
            when(humanPrincipalRepository.save(any())).thenReturn(principal);
            when(principalEventMapper.toPrincipalDeactivatedEvent(any(), any())).thenReturn(
                    new PrincipalDeactivatedEvent(principalId, "HUMAN", "INACTIVE", "Account terminated", LocalDate.now())
            );

            // when
            PrincipalDeactivated result = principalService.deactivatePrincipal(command);

            // then
            assertThat(result).isNotNull();
            assertThat(principal.getStatus()).isEqualTo(PrincipalStatus.INACTIVE);
        }

        @Test
        @DisplayName("should throw exception when principal is PENDING")
        void shouldThrowExceptionWhenPending() {
            // given
            UUID principalId = UUID.randomUUID();
            HumanPrincipalEntity principal = createHumanPrincipal(principalId);
            principal.setStatus(PrincipalStatus.PENDING);

            DeactivatePrincipalCommand command = new DeactivatePrincipalCommand(principalId, "Reason", null);

            when(humanPrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.of(principal));

            // when / then
            assertThatThrownBy(() -> principalService.deactivatePrincipal(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ACTIVE or SUSPENDED");
        }
    }

    @Nested
    @DisplayName("deletePrincipalGdpr")
    class DeletePrincipalGdpr {

        @Test
        @DisplayName("should delete INACTIVE principal per GDPR")
        void shouldDeleteInactivePrincipalPerGdpr() {
            // given
            UUID principalId = UUID.randomUUID();
            HumanPrincipalEntity principal = createHumanPrincipal(principalId);
            principal.setStatus(PrincipalStatus.INACTIVE);
            principal.setUpdatedAt(Instant.now().minus(Duration.ofDays(31))); // past retention period
            principal.setKeycloakUserId("keycloak-123");

            DeletePrincipalGdprCommand command = new DeletePrincipalGdprCommand(
                    principalId, "DELETE", "GDPR-12345", "requestor@example.com"
            );

            when(humanPrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.of(principal));
            when(humanPrincipalRepository.save(any())).thenReturn(principal);
//            when(membershipRepository.findByPrincipalId(principalId)).thenReturn(List.of());

            // when
            PrincipalDeleted result = principalService.deletePrincipalGdpr(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.anonymized()).isTrue();
            assertThat(result.auditReference()).startsWith("aud-");
            assertThat(principal.getStatus()).isEqualTo(PrincipalStatus.DELETED);
            assertThat(principal.getUsername()).startsWith("deleted_user_");
            verify(keycloakService).deleteUser("keycloak-123");
        }

        @Test
        @DisplayName("should throw exception when principal is not INACTIVE")
        void shouldThrowExceptionWhenNotInactive() {
            // given
            UUID principalId = UUID.randomUUID();
            HumanPrincipalEntity principal = createHumanPrincipal(principalId);
            principal.setStatus(PrincipalStatus.ACTIVE);

            DeletePrincipalGdprCommand command = new DeletePrincipalGdprCommand(
                    principalId, "DELETE", "GDPR-12345", "requestor@example.com"
            );

            when(humanPrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.of(principal));

            // when / then
            assertThatThrownBy(() -> principalService.deletePrincipalGdpr(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("INACTIVE");
        }

        @Test
        @DisplayName("should throw exception when retention period not met")
        void shouldThrowExceptionWhenRetentionNotMet() {
            // given
            UUID principalId = UUID.randomUUID();
            HumanPrincipalEntity principal = createHumanPrincipal(principalId);
            principal.setStatus(PrincipalStatus.INACTIVE);
            principal.setUpdatedAt(Instant.now()); // just updated, retention not met

            DeletePrincipalGdprCommand command = new DeletePrincipalGdprCommand(
                    principalId, "DELETE", "GDPR-12345", "requestor@example.com"
            );

            when(humanPrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.of(principal));

            // when / then
            assertThatThrownBy(() -> principalService.deletePrincipalGdpr(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("30 days");
        }
    }

    @Nested
    @DisplayName("getCredentialStatus")
    class GetCredentialStatus {

        @Test
        @DisplayName("should return credential status for service principal")
        void shouldReturnCredentialStatusForServicePrincipal() {
            // given
            UUID principalId = UUID.randomUUID();
            ServicePrincipalEntity principal = createServicePrincipal(principalId);
            principal.setApiKeyHash("someHash");
            principal.setCredentialRotationDate(LocalDate.now().plusDays(30));

            when(servicePrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.of(principal));

            CredentialStatus expectedStatus = new CredentialStatus(
                    principalId, "SERVICE", LocalDate.now().plusDays(30), 30L,
                    null, false, true, false
            );
            when(servicePrincipalMapper.toCredentialStatus(any(ServicePrincipalEntity.class))).thenReturn(expectedStatus);

            // when
            CredentialStatus result = principalService.getCredentialStatus(PrincipalId.of(principalId));

            // then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(principalId);
            assertThat(result.principalType()).isEqualTo("SERVICE");
            assertThat(result.hasApiKey()).isTrue();
            assertThat(result.daysUntilRotation()).isEqualTo(30);
            assertThat(result.rotationRequired()).isFalse();
        }

        @Test
        @DisplayName("should throw exception for human principal")
        void shouldThrowExceptionForHumanPrincipal() {
            // given
            UUID principalId = UUID.randomUUID();
            HumanPrincipalEntity principal = createHumanPrincipal(principalId);

            when(humanPrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.of(principal));

            // when / then
            assertThatThrownBy(() -> principalService.getCredentialStatus(PrincipalId.of(principalId)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("SERVICE or SYSTEM");
        }
    }

    @Nested
    @DisplayName("updateHeartbeat")
    class UpdateHeartbeat {

        @Test
        @DisplayName("should update device heartbeat successfully")
        void shouldUpdateHeartbeatSuccessfully() {
            // given
            UUID principalId = UUID.randomUUID();
            DevicePrincipalEntity device = createDevicePrincipal(principalId);

            UpdateHeartbeatCommand command = new UpdateHeartbeatCommand(
                    principalId, "v2.0.0", Map.of("location", "New location")
            );

            when(devicePrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.of(device));
            when(devicePrincipalRepository.save(any())).thenReturn(device);

            // when
            HeartbeatUpdated result = principalService.updateHeartbeat(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(principalId);
            assertThat(result.lastHeartbeatAt()).isNotNull();
            assertThat(device.getFirmwareVersion()).isEqualTo("v2.0.0");
            assertThat(device.getLocationInfo()).containsEntry("location", "New location");
        }

        @Test
        @DisplayName("should throw exception when device not found")
        void shouldThrowExceptionWhenDeviceNotFound() {
            // given
            UUID principalId = UUID.randomUUID();
            UpdateHeartbeatCommand command = new UpdateHeartbeatCommand(principalId, null, null);

            when(devicePrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> principalService.updateHeartbeat(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("not found");
        }
    }

    @Nested
    @DisplayName("updateCommonAttributes")
    class UpdateCommonAttributes {

        @Test
        @DisplayName("should update context tags successfully")
        void shouldUpdateContextTagsSuccessfully() {
            // given
            UUID principalId = UUID.randomUUID();
            HumanPrincipalEntity principal = createHumanPrincipal(principalId);

            UpdateCommonAttributesCommand command = new UpdateCommonAttributesCommand(
                    principalId, Map.of("tag1", "value1", "tag2", "value2")
            );

            when(humanPrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.of(principal));
            when(humanPrincipalRepository.save(any())).thenReturn(principal);

            // when
            CommonAttributesUpdated result = principalService.updateCommonAttributes(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(principalId);
            assertThat(principal.getContextTags()).containsEntry("tag1", "value1").containsEntry("tag2", "value2");
        }
    }

    // Helper methods

    private HumanPrincipalEntity createHumanPrincipal(UUID principalId) {
        HumanPrincipalEntity entity = new HumanPrincipalEntity();
        entity.setBusinessId(PrincipalId.of(principalId));
        entity.setUsername("testuser");
        entity.setEmail("test@example.com");
        entity.setFirstName("Test");
        entity.setLastName("User");
        entity.setDisplayName("Test User");
        entity.setStatus(PrincipalStatus.ACTIVE);
        return entity;
    }

    private ServicePrincipalEntity createServicePrincipal(UUID principalId) {
        ServicePrincipalEntity entity = new ServicePrincipalEntity();
        entity.setBusinessId(PrincipalId.of(principalId));
        entity.setUsername("testservice");
        entity.setServiceName("TestService");
        entity.setStatus(PrincipalStatus.ACTIVE);
        entity.setCredentialRotationDate(LocalDate.now().plusDays(90));
        return entity;
    }

    private DevicePrincipalEntity createDevicePrincipal(UUID principalId) {
        DevicePrincipalEntity entity = new DevicePrincipalEntity();
        entity.setBusinessId(PrincipalId.of(principalId));
        entity.setUsername("testdevice");
        entity.setDeviceIdentifier("TEST_DEVICE");
        entity.setDeviceType(DeviceType.IOT_SENSOR);
        entity.setStatus(PrincipalStatus.ACTIVE);
        return entity;
    }
}
