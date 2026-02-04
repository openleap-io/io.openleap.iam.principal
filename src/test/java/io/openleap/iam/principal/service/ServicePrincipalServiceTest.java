package io.openleap.iam.principal.service;

import io.openleap.iam.principal.domain.dto.CreateServicePrincipalCommand;
import io.openleap.iam.principal.domain.dto.CredentialsRotated;
import io.openleap.iam.principal.domain.dto.RotateCredentialsCommand;
import io.openleap.iam.principal.domain.dto.ServicePrincipalCreated;
import io.openleap.iam.principal.domain.entity.PrincipalId;
import io.openleap.iam.principal.domain.entity.PrincipalStatus;
import io.openleap.iam.principal.domain.entity.ServicePrincipalEntity;
import io.openleap.iam.principal.domain.entity.SyncStatus;
import io.openleap.iam.principal.domain.event.CredentialsRotatedEvent;
import io.openleap.iam.principal.domain.event.ServicePrincipalCreatedEvent;
import io.openleap.iam.principal.domain.mapper.ServicePrincipalMapper;
import io.openleap.iam.principal.exception.ServiceNameAlreadyExistsException;
import io.openleap.iam.principal.exception.UsernameAlreadyExistsException;
import io.openleap.iam.principal.repository.ServicePrincipalRepository;
import io.openleap.iam.principal.service.keycloak.KeycloakService;
import io.openleap.common.messaging.event.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("ServicePrincipalService Unit Tests")
@ExtendWith(MockitoExtension.class)
class ServicePrincipalServiceTest {

    @Mock
    private ServicePrincipalRepository servicePrincipalRepository;

    @Mock
    private KeycloakService keycloakService;

    @Mock
    private CredentialService credentialService;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private ServicePrincipalMapper servicePrincipalMapper;

    private ServicePrincipalService servicePrincipalService;

    @BeforeEach
    void setUp() {
        servicePrincipalService = new ServicePrincipalService(
                servicePrincipalRepository,
                keycloakService,
                credentialService,
                eventPublisher,
                servicePrincipalMapper
        );
    }

    @Nested
    @DisplayName("createServicePrincipal")
    class CreateServicePrincipal {

        @Test
        @DisplayName("should create service principal successfully")
        void shouldCreateServicePrincipalSuccessfully() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateServicePrincipalCommand command = new CreateServicePrincipalCommand(
                    "PaymentService",
                    tenantId,
                    Map.of("env", "production"),
                    List.of("payments.read", "payments.write")
            );

            when(servicePrincipalRepository.existsByServiceName("PaymentService")).thenReturn(false);
            when(servicePrincipalRepository.existsByUsername("paymentservice")).thenReturn(false);
            when(credentialService.generateApiKey()).thenReturn("sk_live_testApiKey123");
            when(credentialService.hashApiKey("sk_live_testApiKey123")).thenReturn("hashedApiKey");
            when(keycloakService.createClient(anyString(), anyList())).thenReturn("keycloak-client-secret");

            ServicePrincipalEntity savedEntity = createServicePrincipalEntity("paymentservice", "PaymentService");
            when(servicePrincipalRepository.save(any(ServicePrincipalEntity.class))).thenReturn(savedEntity);

            ServicePrincipalCreatedEvent mockEvent = new ServicePrincipalCreatedEvent(
                    savedEntity.getBusinessId().value(), "SERVICE", "paymentservice", "PaymentService",
                    tenantId, "ACTIVE", List.of("payments.read", "payments.write"),
                    LocalDate.now().plusDays(90), null
            );
            when(servicePrincipalMapper.toServicePrincipalCreatedEvent(any())).thenReturn(mockEvent);

            ServicePrincipalCreated expectedResult = new ServicePrincipalCreated(
                    savedEntity.getBusinessId().value(), "paymentservice", "PaymentService",
                    "sk_live_testApiKey123", "PaymentService", "keycloak-client-secret",
                    List.of("payments.read", "payments.write"), LocalDate.now().plusDays(90)
            );
            when(servicePrincipalMapper.toServicePrincipalCreated(any(), anyString(), anyString(), anyString()))
                    .thenReturn(expectedResult);

            // when
            ServicePrincipalCreated result = servicePrincipalService.createServicePrincipal(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(savedEntity.getBusinessId().value());
            assertThat(result.username()).isEqualTo("paymentservice");
            assertThat(result.serviceName()).isEqualTo("PaymentService");
            assertThat(result.apiKey()).isEqualTo("sk_live_testApiKey123");
            assertThat(result.keycloakClientSecret()).isEqualTo("keycloak-client-secret");
            verify(credentialService).generateApiKey();
            verify(credentialService).hashApiKey("sk_live_testApiKey123");
            verify(keycloakService).createClient(eq("PaymentService"), anyList());
            verify(servicePrincipalMapper).toServicePrincipalCreatedEvent(any());
            verify(servicePrincipalMapper).toServicePrincipalCreated(any(), eq("sk_live_testApiKey123"), eq("PaymentService"), eq("keycloak-client-secret"));
        }

        @Test
        @DisplayName("should throw ServiceNameAlreadyExistsException when service name exists")
        void shouldThrowExceptionWhenServiceNameExists() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateServicePrincipalCommand command = new CreateServicePrincipalCommand(
                    "ExistingService",
                    tenantId,
                    null,
                    List.of("read")
            );

            when(servicePrincipalRepository.existsByServiceName("ExistingService")).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> servicePrincipalService.createServicePrincipal(command))
                    .isInstanceOf(ServiceNameAlreadyExistsException.class)
                    .hasMessageContaining("ExistingService");

            verify(servicePrincipalRepository, never()).save(any());
            verify(keycloakService, never()).createClient(any(), any());
        }

        @Test
        @DisplayName("should throw UsernameAlreadyExistsException when derived username exists")
        void shouldThrowExceptionWhenUsernameExists() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateServicePrincipalCommand command = new CreateServicePrincipalCommand(
                    "NewService",
                    tenantId,
                    null,
                    List.of("read")
            );

            when(servicePrincipalRepository.existsByServiceName("NewService")).thenReturn(false);
            when(servicePrincipalRepository.existsByUsername("newservice")).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> servicePrincipalService.createServicePrincipal(command))
                    .isInstanceOf(UsernameAlreadyExistsException.class)
                    .hasMessageContaining("newservice");

            verify(servicePrincipalRepository, never()).save(any());
        }

//        @Test
//        @DisplayName("should throw TenantNotFoundException when tenant does not exist")
//        void shouldThrowExceptionWhenTenantNotFound() {
//            // given
//            UUID tenantId = UUID.randomUUID();
//            CreateServicePrincipalCommand command = new CreateServicePrincipalCommand(
//                    "NewService",
//                    tenantId,
//                    null,
//                    List.of("read")
//            );
//
//            when(servicePrincipalRepository.existsByServiceName("NewService")).thenReturn(false);
//            when(servicePrincipalRepository.existsByUsername("newservice")).thenReturn(false);
//            when(tenantService.tenantExists(tenantId)).thenReturn(false);
//
//            // when / then
//            assertThatThrownBy(() -> servicePrincipalService.createServicePrincipal(command))
//                    .isInstanceOf(TenantNotFoundException.class);
//
//            verify(servicePrincipalRepository, never()).save(any());
//        }

        @Test
        @DisplayName("should rollback when Keycloak client creation fails")
        void shouldRollbackWhenKeycloakFails() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateServicePrincipalCommand command = new CreateServicePrincipalCommand(
                    "NewService",
                    tenantId,
                    null,
                    List.of("read")
            );

            when(servicePrincipalRepository.existsByServiceName("NewService")).thenReturn(false);
            when(servicePrincipalRepository.existsByUsername("newservice")).thenReturn(false);
//            when(tenantService.tenantExists(tenantId)).thenReturn(true);
            when(credentialService.generateApiKey()).thenReturn("sk_live_test");
            when(credentialService.hashApiKey(any())).thenReturn("hash");
            when(servicePrincipalRepository.save(any())).thenReturn(createServicePrincipalEntity("newservice", "NewService"));
            when(keycloakService.createClient(any(), any())).thenThrow(new RuntimeException("Keycloak error"));

            // when / then
            assertThatThrownBy(() -> servicePrincipalService.createServicePrincipal(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Keycloak");
        }

        @Test
        @DisplayName("should set initial status to ACTIVE")
        void shouldSetInitialStatusToActive() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateServicePrincipalCommand command = new CreateServicePrincipalCommand(
                    "NewService",
                    tenantId,
                    null,
                    List.of("read")
            );

            when(servicePrincipalRepository.existsByServiceName("NewService")).thenReturn(false);
            when(servicePrincipalRepository.existsByUsername("newservice")).thenReturn(false);
            when(credentialService.generateApiKey()).thenReturn("sk_live_test");
            when(credentialService.hashApiKey(any())).thenReturn("hash");
            when(keycloakService.createClient(any(), any())).thenReturn("secret");
            when(servicePrincipalMapper.toServicePrincipalCreatedEvent(any())).thenReturn(mock(ServicePrincipalCreatedEvent.class));
            when(servicePrincipalMapper.toServicePrincipalCreated(any(), any(), any(), any())).thenReturn(mock(ServicePrincipalCreated.class));

            ArgumentCaptor<ServicePrincipalEntity> captor = ArgumentCaptor.forClass(ServicePrincipalEntity.class);
            when(servicePrincipalRepository.save(captor.capture())).thenAnswer(inv -> {
                ServicePrincipalEntity entity = inv.getArgument(0);
                entity.setBusinessId(PrincipalId.of(UUID.randomUUID()));
                return entity;
            });

            // when
            servicePrincipalService.createServicePrincipal(command);

            // then
            ServicePrincipalEntity savedEntity = captor.getValue();
            assertThat(savedEntity.getStatus()).isEqualTo(PrincipalStatus.ACTIVE);
        }

        @Test
        @DisplayName("should set credential rotation date 90 days from now")
        void shouldSetCredentialRotationDate() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateServicePrincipalCommand command = new CreateServicePrincipalCommand(
                    "NewService",
                    tenantId,
                    null,
                    List.of("read")
            );

            when(servicePrincipalRepository.existsByServiceName("NewService")).thenReturn(false);
            when(servicePrincipalRepository.existsByUsername("newservice")).thenReturn(false);
            when(credentialService.generateApiKey()).thenReturn("sk_live_test");
            when(credentialService.hashApiKey(any())).thenReturn("hash");
            when(keycloakService.createClient(any(), any())).thenReturn("secret");
            when(servicePrincipalMapper.toServicePrincipalCreatedEvent(any())).thenReturn(mock(ServicePrincipalCreatedEvent.class));
            when(servicePrincipalMapper.toServicePrincipalCreated(any(), any(), any(), any())).thenReturn(mock(ServicePrincipalCreated.class));

            ArgumentCaptor<ServicePrincipalEntity> captor = ArgumentCaptor.forClass(ServicePrincipalEntity.class);
            when(servicePrincipalRepository.save(captor.capture())).thenAnswer(inv -> {
                ServicePrincipalEntity entity = inv.getArgument(0);
                entity.setBusinessId(PrincipalId.of(UUID.randomUUID()));
                return entity;
            });

            // when
            servicePrincipalService.createServicePrincipal(command);

            // then
            ServicePrincipalEntity savedEntity = captor.getValue();
            LocalDate expectedDate = LocalDate.now().plusDays(90);
            assertThat(savedEntity.getCredentialRotationDate()).isEqualTo(expectedDate);
        }

        @Test
        @DisplayName("should convert service name to lowercase for username")
        void shouldConvertServiceNameToLowercaseForUsername() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateServicePrincipalCommand command = new CreateServicePrincipalCommand(
                    "MyServiceName",
                    tenantId,
                    null,
                    List.of("read")
            );

            when(servicePrincipalRepository.existsByServiceName("MyServiceName")).thenReturn(false);
            when(servicePrincipalRepository.existsByUsername("myservicename")).thenReturn(false);
            when(credentialService.generateApiKey()).thenReturn("sk_live_test");
            when(credentialService.hashApiKey(any())).thenReturn("hash");
            when(keycloakService.createClient(any(), any())).thenReturn("secret");
            when(servicePrincipalMapper.toServicePrincipalCreatedEvent(any())).thenReturn(mock(ServicePrincipalCreatedEvent.class));
            when(servicePrincipalMapper.toServicePrincipalCreated(any(), any(), any(), any())).thenReturn(mock(ServicePrincipalCreated.class));

            ArgumentCaptor<ServicePrincipalEntity> captor = ArgumentCaptor.forClass(ServicePrincipalEntity.class);
            when(servicePrincipalRepository.save(captor.capture())).thenAnswer(inv -> {
                ServicePrincipalEntity entity = inv.getArgument(0);
                entity.setBusinessId(PrincipalId.of(UUID.randomUUID()));
                return entity;
            });

            // when
            servicePrincipalService.createServicePrincipal(command);

            // then
            ServicePrincipalEntity savedEntity = captor.getValue();
            assertThat(savedEntity.getUsername()).isEqualTo("myservicename");
        }
    }

    @Nested
    @DisplayName("rotateCredentials")
    class RotateCredentials {

        @Test
        @DisplayName("should rotate credentials when rotation is due")
        void shouldRotateCredentialsWhenDue() {
            // given
            UUID principalId = UUID.randomUUID();
            ServicePrincipalEntity existingPrincipal = createActiveServicePrincipal(principalId);
            existingPrincipal.setCredentialRotationDate(LocalDate.now().minusDays(1)); // past due
            existingPrincipal.setKeycloakClientId("keycloak-client-id");

            RotateCredentialsCommand command = new RotateCredentialsCommand(principalId, false, "Scheduled rotation");

            when(servicePrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.of(existingPrincipal));
            when(credentialService.generateApiKey()).thenReturn("sk_live_newApiKey");
            when(credentialService.hashApiKey("sk_live_newApiKey")).thenReturn("newHash");
            when(keycloakService.regenerateClientSecret("keycloak-client-id")).thenReturn("new-keycloak-secret");
            when(servicePrincipalRepository.save(any())).thenReturn(existingPrincipal);

            when(servicePrincipalMapper.toCredentialsRotatedEvent(any(), any(), any(), any()))
                    .thenReturn(mock(CredentialsRotatedEvent.class));

            LocalDate expectedRotationDate = LocalDate.now().plusDays(90);
            CredentialsRotated expectedResult = new CredentialsRotated(
                    principalId, "sk_live_newApiKey", "new-keycloak-secret", expectedRotationDate, java.time.Instant.now()
            );
            when(servicePrincipalMapper.toCredentialsRotated(any(), eq("sk_live_newApiKey"), eq("new-keycloak-secret"), any(), any()))
                    .thenReturn(expectedResult);

            // when
            CredentialsRotated result = servicePrincipalService.rotateCredentials(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(principalId);
            assertThat(result.apiKey()).isEqualTo("sk_live_newApiKey");
            assertThat(result.keycloakClientSecret()).isEqualTo("new-keycloak-secret");
            assertThat(result.credentialRotationDate()).isEqualTo(expectedRotationDate);
            verify(credentialService).generateApiKey();
            verify(keycloakService).regenerateClientSecret("keycloak-client-id");
            verify(servicePrincipalMapper).toCredentialsRotatedEvent(any(), eq(command), any(), any());
            verify(servicePrincipalMapper).toCredentialsRotated(any(), eq("sk_live_newApiKey"), eq("new-keycloak-secret"), any(), any());
        }

        @Test
        @DisplayName("should rotate credentials when forced")
        void shouldRotateCredentialsWhenForced() {
            // given
            UUID principalId = UUID.randomUUID();
            ServicePrincipalEntity existingPrincipal = createActiveServicePrincipal(principalId);
            existingPrincipal.setCredentialRotationDate(LocalDate.now().plusDays(30)); // not due yet
            existingPrincipal.setKeycloakClientId("keycloak-client-id");

            RotateCredentialsCommand command = new RotateCredentialsCommand(principalId, true, "Security incident");

            when(servicePrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.of(existingPrincipal));
            when(credentialService.generateApiKey()).thenReturn("sk_live_newApiKey");
            when(credentialService.hashApiKey("sk_live_newApiKey")).thenReturn("newHash");
            when(keycloakService.regenerateClientSecret("keycloak-client-id")).thenReturn("new-keycloak-secret");
            when(servicePrincipalRepository.save(any())).thenReturn(existingPrincipal);

            when(servicePrincipalMapper.toCredentialsRotatedEvent(any(), any(), any(), any()))
                    .thenReturn(mock(CredentialsRotatedEvent.class));

            CredentialsRotated expectedResult = new CredentialsRotated(
                    principalId, "sk_live_newApiKey", "new-keycloak-secret", LocalDate.now().plusDays(90), java.time.Instant.now()
            );
            when(servicePrincipalMapper.toCredentialsRotated(any(), any(), any(), any(), any()))
                    .thenReturn(expectedResult);

            // when
            CredentialsRotated result = servicePrincipalService.rotateCredentials(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.apiKey()).isEqualTo("sk_live_newApiKey");
        }

        @Test
        @DisplayName("should throw exception when principal not found")
        void shouldThrowExceptionWhenPrincipalNotFound() {
            // given
            UUID principalId = UUID.randomUUID();
            RotateCredentialsCommand command = new RotateCredentialsCommand(principalId, false, "Test");

            when(servicePrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> servicePrincipalService.rotateCredentials(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("should throw exception when principal is not ACTIVE")
        void shouldThrowExceptionWhenPrincipalNotActive() {
            // given
            UUID principalId = UUID.randomUUID();
            ServicePrincipalEntity inactivePrincipal = createActiveServicePrincipal(principalId);
            inactivePrincipal.setStatus(PrincipalStatus.SUSPENDED);

            RotateCredentialsCommand command = new RotateCredentialsCommand(principalId, false, "Test");

            when(servicePrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.of(inactivePrincipal));

            // when / then
            assertThatThrownBy(() -> servicePrincipalService.rotateCredentials(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ACTIVE");
        }

        @Test
        @DisplayName("should throw exception when rotation not due and not forced")
        void shouldThrowExceptionWhenRotationNotDueAndNotForced() {
            // given
            UUID principalId = UUID.randomUUID();
            ServicePrincipalEntity existingPrincipal = createActiveServicePrincipal(principalId);
            existingPrincipal.setCredentialRotationDate(LocalDate.now().plusDays(30)); // not due

            RotateCredentialsCommand command = new RotateCredentialsCommand(principalId, false, "Test");

            when(servicePrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.of(existingPrincipal));

            // when / then
            assertThatThrownBy(() -> servicePrincipalService.rotateCredentials(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not due");
        }

        @Test
        @DisplayName("should throw exception when Keycloak secret regeneration fails")
        void shouldThrowExceptionWhenKeycloakFails() {
            // given
            UUID principalId = UUID.randomUUID();
            ServicePrincipalEntity existingPrincipal = createActiveServicePrincipal(principalId);
            existingPrincipal.setCredentialRotationDate(LocalDate.now().minusDays(1));
            existingPrincipal.setKeycloakClientId("keycloak-client-id");

            RotateCredentialsCommand command = new RotateCredentialsCommand(principalId, false, "Test");

            when(servicePrincipalRepository.findByBusinessId(PrincipalId.of(principalId))).thenReturn(Optional.of(existingPrincipal));
            when(credentialService.generateApiKey()).thenReturn("sk_live_newApiKey");
            when(credentialService.hashApiKey(any())).thenReturn("hash");
            when(servicePrincipalRepository.save(any())).thenReturn(existingPrincipal);
            when(keycloakService.regenerateClientSecret(any())).thenThrow(new RuntimeException("Keycloak error"));

            // when / then
            assertThatThrownBy(() -> servicePrincipalService.rotateCredentials(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Keycloak");
        }
    }

    // Helper methods

    private ServicePrincipalEntity createServicePrincipalEntity(String username, String serviceName) {
        ServicePrincipalEntity entity = new ServicePrincipalEntity();
        entity.setBusinessId(PrincipalId.of(UUID.randomUUID()));
        entity.setUsername(username);
        entity.setServiceName(serviceName);
        entity.setStatus(PrincipalStatus.ACTIVE);
        entity.setSyncStatus(SyncStatus.PENDING);
        entity.setCredentialRotationDate(LocalDate.now().plusDays(90));
        return entity;
    }

    private ServicePrincipalEntity createActiveServicePrincipal(UUID principalId) {
        ServicePrincipalEntity entity = new ServicePrincipalEntity();
        entity.setBusinessId(PrincipalId.of(principalId));
        entity.setUsername("testservice");
        entity.setServiceName("TestService");
        entity.setStatus(PrincipalStatus.ACTIVE);
        entity.setSyncStatus(SyncStatus.SYNCED);
        entity.setApiKeyHash("existingHash");
        entity.setCredentialRotationDate(LocalDate.now().plusDays(90));
        return entity;
    }
}
