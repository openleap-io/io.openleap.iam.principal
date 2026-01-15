package io.openleap.iam.principal.service;

import io.openleap.iam.principal.domain.dto.CreateHumanPrincipalCommand;
import io.openleap.iam.principal.domain.dto.HumanPrincipalCreated;
import io.openleap.iam.principal.domain.dto.ProfileDetails;
import io.openleap.iam.principal.domain.dto.ProfileUpdated;
import io.openleap.iam.principal.domain.dto.UpdateProfileCommand;
import io.openleap.iam.principal.domain.entity.HumanPrincipalEntity;
import io.openleap.iam.principal.domain.entity.PrincipalStatus;
import io.openleap.iam.principal.domain.entity.SyncStatus;
import io.openleap.iam.principal.exception.EmailAlreadyExistsException;
import io.openleap.iam.principal.exception.InactivePrincipalFoundException;
import io.openleap.iam.principal.exception.TenantNotFoundException;
import io.openleap.iam.principal.exception.UsernameAlreadyExistsException;
import io.openleap.iam.principal.repository.HumanPrincipalRepository;
import io.openleap.iam.principal.repository.PrincipalTenantMembershipRepository;
import io.openleap.iam.principal.service.keycloak.KeycloakService;
import io.openleap.starter.core.messaging.event.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("HumanPrincipalService Unit Tests")
@ExtendWith(MockitoExtension.class)
class HumanPrincipalServiceTest {

    @Mock
    private HumanPrincipalRepository humanPrincipalRepository;

    @Mock
    private PrincipalTenantMembershipRepository membershipRepository;

    @Mock
    private TenantService tenantService;

    @Mock
    private KeycloakService keycloakService;

    @Mock
    private EventPublisher eventPublisher;

    private HumanPrincipalService humanPrincipalService;

    @BeforeEach
    void setUp() {
        humanPrincipalService = new HumanPrincipalService(
                humanPrincipalRepository,
                membershipRepository,
                tenantService,
                keycloakService,
                eventPublisher
        );
    }

    @Nested
    @DisplayName("createHumanPrincipal")
    class CreateHumanPrincipal {

        @Test
        @DisplayName("should create human principal successfully")
        void shouldCreateHumanPrincipalSuccessfully() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateHumanPrincipalCommand command = new CreateHumanPrincipalCommand(
                    "johndoe",
                    "john@example.com",
                    tenantId,
                    Map.of("developer", "true", "admin", "true"),
                    "John Doe",
                    "John",
                    "Doe",
                    "+1234567890",
                    "en",
                    "America/New_York",
                    "en-US",
                    "https://example.com/avatar.jpg",
                    "A software developer",
                    Map.of("theme", "dark")
            );

            when(humanPrincipalRepository.existsByUsername("johndoe")).thenReturn(false);
            when(humanPrincipalRepository.findInactiveByEmail("john@example.com")).thenReturn(Optional.empty());
            when(humanPrincipalRepository.existsByEmail("john@example.com")).thenReturn(false);
            when(tenantService.tenantExists(tenantId)).thenReturn(true);
            when(keycloakService.createUser(any())).thenReturn("keycloak-user-id");

            HumanPrincipalEntity savedEntity = createHumanPrincipalEntity("johndoe", "john@example.com");
            when(humanPrincipalRepository.save(any(HumanPrincipalEntity.class))).thenReturn(savedEntity);

            // when
            HumanPrincipalCreated result = humanPrincipalService.createHumanPrincipal(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.principalId()).isEqualTo(savedEntity.getPrincipalId());
            verify(humanPrincipalRepository, times(2)).save(any(HumanPrincipalEntity.class));
            verify(membershipRepository).save(any());
            verify(keycloakService).createUser(any());
        }

        @Test
        @DisplayName("should throw UsernameAlreadyExistsException when username exists")
        void shouldThrowExceptionWhenUsernameExists() {
            // given
            CreateHumanPrincipalCommand command = createCommand("existinguser", "new@example.com");
            when(humanPrincipalRepository.existsByUsername("existinguser")).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> humanPrincipalService.createHumanPrincipal(command))
                    .isInstanceOf(UsernameAlreadyExistsException.class)
                    .hasMessageContaining("existinguser");

            verify(humanPrincipalRepository, never()).save(any());
            verify(keycloakService, never()).createUser(any());
        }

        @Test
        @DisplayName("should throw EmailAlreadyExistsException when email exists")
        void shouldThrowExceptionWhenEmailExists() {
            // given
            CreateHumanPrincipalCommand command = createCommand("newuser", "existing@example.com");
            when(humanPrincipalRepository.existsByUsername("newuser")).thenReturn(false);
            when(humanPrincipalRepository.findInactiveByEmail("existing@example.com")).thenReturn(Optional.empty());
            when(humanPrincipalRepository.existsByEmail("existing@example.com")).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> humanPrincipalService.createHumanPrincipal(command))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessageContaining("existing@example.com");

            verify(humanPrincipalRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw InactivePrincipalFoundException when inactive principal found")
        void shouldThrowExceptionWhenInactivePrincipalFound() {
            // given
            CreateHumanPrincipalCommand command = createCommand("newuser", "inactive@example.com");
            HumanPrincipalEntity inactivePrincipal = createHumanPrincipalEntity("olduser", "inactive@example.com");
            inactivePrincipal.setStatus(PrincipalStatus.INACTIVE);

            when(humanPrincipalRepository.existsByUsername("newuser")).thenReturn(false);
            when(humanPrincipalRepository.findInactiveByEmail("inactive@example.com"))
                    .thenReturn(Optional.of(inactivePrincipal));

            // when / then
            assertThatThrownBy(() -> humanPrincipalService.createHumanPrincipal(command))
                    .isInstanceOf(InactivePrincipalFoundException.class);

            verify(humanPrincipalRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw TenantNotFoundException when tenant does not exist")
        void shouldThrowExceptionWhenTenantNotFound() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateHumanPrincipalCommand command = createCommand("newuser", "new@example.com", tenantId);
            when(humanPrincipalRepository.existsByUsername("newuser")).thenReturn(false);
            when(humanPrincipalRepository.findInactiveByEmail("new@example.com")).thenReturn(Optional.empty());
            when(humanPrincipalRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(tenantService.tenantExists(tenantId)).thenReturn(false);

            // when / then
            assertThatThrownBy(() -> humanPrincipalService.createHumanPrincipal(command))
                    .isInstanceOf(TenantNotFoundException.class);

            verify(humanPrincipalRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when Keycloak sync fails")
        void shouldThrowExceptionWhenKeycloakFails() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateHumanPrincipalCommand command = createCommand("newuser", "new@example.com", tenantId);
            when(humanPrincipalRepository.existsByUsername("newuser")).thenReturn(false);
            when(humanPrincipalRepository.findInactiveByEmail("new@example.com")).thenReturn(Optional.empty());
            when(humanPrincipalRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(tenantService.tenantExists(tenantId)).thenReturn(true);

            ArgumentCaptor<HumanPrincipalEntity> captor = ArgumentCaptor.forClass(HumanPrincipalEntity.class);
            when(humanPrincipalRepository.save(captor.capture())).thenAnswer(inv -> {
                HumanPrincipalEntity entity = inv.getArgument(0);
                entity.setPrincipalId(UUID.randomUUID());
                return entity;
            });

            when(keycloakService.createUser(any())).thenThrow(new RuntimeException("Keycloak error"));

            // when / then
            assertThatThrownBy(() -> humanPrincipalService.createHumanPrincipal(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Keycloak");
        }

        @Test
        @DisplayName("should set initial status to PENDING")
        void shouldSetInitialStatusToPending() {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateHumanPrincipalCommand command = createCommand("newuser", "new@example.com", tenantId);
            when(humanPrincipalRepository.existsByUsername("newuser")).thenReturn(false);
            when(humanPrincipalRepository.findInactiveByEmail("new@example.com")).thenReturn(Optional.empty());
            when(humanPrincipalRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(tenantService.tenantExists(tenantId)).thenReturn(true);
            when(keycloakService.createUser(any())).thenReturn("keycloak-user-id");

            ArgumentCaptor<HumanPrincipalEntity> captor = ArgumentCaptor.forClass(HumanPrincipalEntity.class);
            when(humanPrincipalRepository.save(captor.capture())).thenAnswer(inv -> {
                HumanPrincipalEntity entity = inv.getArgument(0);
                entity.setPrincipalId(UUID.randomUUID());
                return entity;
            });

            // when
            humanPrincipalService.createHumanPrincipal(command);

            // then
            HumanPrincipalEntity firstSave = captor.getAllValues().get(0);
            assertThat(firstSave.getStatus()).isEqualTo(PrincipalStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("updateProfile")
    class UpdateProfile {

        @Test
        @DisplayName("should update profile successfully")
        void shouldUpdateProfileSuccessfully() {
            // given
            UUID principalId = UUID.randomUUID();
            HumanPrincipalEntity existingPrincipal = createActivePrincipal(principalId);
            existingPrincipal.setKeycloakUserId("keycloak-123");

            UpdateProfileCommand command = new UpdateProfileCommand(
                    principalId,
                    "UpdatedFirstName",
                    "UpdatedLastName",
                    "Updated Display",
                    "+9876543210",
                    "de",
                    "Europe/Berlin",
                    "de-DE",
                    "https://new-avatar.com/img.jpg",
                    "Updated bio",
                    Map.of("newPref", "value"),
                    Map.of("newTag", "value")
            );

            when(humanPrincipalRepository.findByPrincipalId(principalId)).thenReturn(Optional.of(existingPrincipal));
            when(humanPrincipalRepository.save(any())).thenReturn(existingPrincipal);

            // when
            ProfileUpdated result = humanPrincipalService.updateProfile(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.principalId()).isEqualTo(principalId);
            assertThat(result.changedFields()).isNotEmpty();
            verify(humanPrincipalRepository).save(any());
            verify(keycloakService).updateUser(eq("keycloak-123"), any());
        }

        @Test
        @DisplayName("should throw exception when principal not found")
        void shouldThrowExceptionWhenPrincipalNotFound() {
            // given
            UUID principalId = UUID.randomUUID();
            UpdateProfileCommand command = new UpdateProfileCommand(
                    principalId, null, null, null, null, null, null, null, null, null, null, null
            );
            when(humanPrincipalRepository.findByPrincipalId(principalId)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> humanPrincipalService.updateProfile(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Principal not found");
        }

        @Test
        @DisplayName("should throw exception when principal is not ACTIVE")
        void shouldThrowExceptionWhenPrincipalNotActive() {
            // given
            UUID principalId = UUID.randomUUID();
            HumanPrincipalEntity inactivePrincipal = createHumanPrincipalEntity("user", "user@test.com");
            inactivePrincipal.setPrincipalId(principalId);
            inactivePrincipal.setStatus(PrincipalStatus.PENDING);

            UpdateProfileCommand command = new UpdateProfileCommand(
                    principalId, "New", null, null, null, null, null, null, null, null, null, null
            );
            when(humanPrincipalRepository.findByPrincipalId(principalId)).thenReturn(Optional.of(inactivePrincipal));

            // when / then
            assertThatThrownBy(() -> humanPrincipalService.updateProfile(command))
                    .isInstanceOf(InactivePrincipalFoundException.class);
        }

        @Test
        @DisplayName("should return empty changed fields when no changes made")
        void shouldReturnEmptyChangedFieldsWhenNoChanges() {
            // given
            UUID principalId = UUID.randomUUID();
            HumanPrincipalEntity existingPrincipal = createActivePrincipal(principalId);
            existingPrincipal.setFirstName("John");
            existingPrincipal.setLastName("Doe");

            UpdateProfileCommand command = new UpdateProfileCommand(
                    principalId,
                    "John", // same as existing
                    "Doe",  // same as existing
                    null, null, null, null, null, null, null, null, null
            );

            when(humanPrincipalRepository.findByPrincipalId(principalId)).thenReturn(Optional.of(existingPrincipal));

            // when
            ProfileUpdated result = humanPrincipalService.updateProfile(command);

            // then
            assertThat(result.changedFields()).isEmpty();
            verify(humanPrincipalRepository, never()).save(any());
        }

        @Test
        @DisplayName("should continue even if Keycloak sync fails")
        void shouldContinueEvenIfKeycloakSyncFails() {
            // given
            UUID principalId = UUID.randomUUID();
            HumanPrincipalEntity existingPrincipal = createActivePrincipal(principalId);
            existingPrincipal.setKeycloakUserId("keycloak-123");

            UpdateProfileCommand command = new UpdateProfileCommand(
                    principalId, "NewFirstName", null, null, null, null, null, null, null, null, null, null
            );

            when(humanPrincipalRepository.findByPrincipalId(principalId)).thenReturn(Optional.of(existingPrincipal));
            when(humanPrincipalRepository.save(any())).thenReturn(existingPrincipal);
            doThrow(new RuntimeException("Keycloak error")).when(keycloakService).updateUser(any(), any());

            // when
            ProfileUpdated result = humanPrincipalService.updateProfile(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.changedFields()).contains("first_name");
        }
    }

    @Nested
    @DisplayName("getProfile")
    class GetProfile {

        @Test
        @DisplayName("should return profile details successfully")
        void shouldReturnProfileDetailsSuccessfully() {
            // given
            UUID principalId = UUID.randomUUID();
            HumanPrincipalEntity principal = createActivePrincipal(principalId);
            principal.setFirstName("John");
            principal.setLastName("Doe");
            principal.setDisplayName("Johnny");
            principal.setPhone("+1234567890");
            principal.setLanguage("en");
            principal.setTimezone("America/New_York");
            principal.setLocale("en-US");
            principal.setAvatarUrl("https://avatar.com/img.jpg");
            principal.setBio("Test bio");
            principal.setPreferences(Map.of("theme", "dark"));

            when(humanPrincipalRepository.findByPrincipalId(principalId)).thenReturn(Optional.of(principal));

            // when
            ProfileDetails result = humanPrincipalService.getProfile(principalId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.principalId()).isEqualTo(principalId);
            assertThat(result.firstName()).isEqualTo("John");
            assertThat(result.lastName()).isEqualTo("Doe");
            assertThat(result.displayName()).isEqualTo("Johnny");
            assertThat(result.phone()).isEqualTo("+1234567890");
            assertThat(result.language()).isEqualTo("en");
            assertThat(result.timezone()).isEqualTo("America/New_York");
            assertThat(result.locale()).isEqualTo("en-US");
            assertThat(result.avatarUrl()).isEqualTo("https://avatar.com/img.jpg");
            assertThat(result.bio()).isEqualTo("Test bio");
            assertThat(result.preferences()).containsEntry("theme", "dark");
        }

        @Test
        @DisplayName("should throw exception when principal not found")
        void shouldThrowExceptionWhenPrincipalNotFound() {
            // given
            UUID principalId = UUID.randomUUID();
            when(humanPrincipalRepository.findByPrincipalId(principalId)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> humanPrincipalService.getProfile(principalId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Principal not found");
        }
    }

    // Helper methods

    private CreateHumanPrincipalCommand createCommand(String username, String email) {
        return createCommand(username, email, UUID.randomUUID());
    }

    private CreateHumanPrincipalCommand createCommand(String username, String email, UUID tenantId) {
        return new CreateHumanPrincipalCommand(
                username,
                email,
                tenantId,
                null, // contextTags
                "Display Name",
                "First",
                "Last",
                null, null, null, null, null, null, null
        );
    }

    private HumanPrincipalEntity createHumanPrincipalEntity(String username, String email) {
        HumanPrincipalEntity entity = new HumanPrincipalEntity();
        entity.setPrincipalId(UUID.randomUUID());
        entity.setUsername(username);
        entity.setEmail(email);
        entity.setFirstName("Test");
        entity.setLastName("User");
        entity.setDisplayName("Test User");
        entity.setStatus(PrincipalStatus.PENDING);
        entity.setSyncStatus(SyncStatus.PENDING);
        entity.setPrimaryTenantId(UUID.randomUUID());
        return entity;
    }

    private HumanPrincipalEntity createActivePrincipal(UUID principalId) {
        HumanPrincipalEntity entity = new HumanPrincipalEntity();
        entity.setPrincipalId(principalId);
        entity.setUsername("activeuser");
        entity.setEmail("active@test.com");
        entity.setFirstName("Active");
        entity.setLastName("User");
        entity.setDisplayName("Active User");
        entity.setStatus(PrincipalStatus.ACTIVE);
        entity.setSyncStatus(SyncStatus.SYNCED);
        entity.setPrimaryTenantId(UUID.randomUUID());
        return entity;
    }
}
