package io.openleap.iam.principal.repository;

import io.openleap.iam.principal.domain.entity.HumanPrincipalEntity;
import io.openleap.iam.principal.domain.entity.PrincipalStatus;
import io.openleap.iam.principal.domain.entity.SyncStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("HumanPrincipalRepository Integration Tests")
class HumanPrincipalRepositoryIT {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private HumanPrincipalRepository humanPrincipalRepository;

    private UUID tenantId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("findByUsername")
    class FindByUsername {

        @Test
        @DisplayName("should find principal by username")
        void shouldFindPrincipalByUsername() {
            // given
            HumanPrincipalEntity principal = createPrincipal("johndoe", "john@example.com");
            entityManager.persistAndFlush(principal);

            // when
            Optional<HumanPrincipalEntity> result = humanPrincipalRepository.findByUsername("johndoe");

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getUsername()).isEqualTo("johndoe");
            assertThat(result.get().getEmail()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("should return empty when username not found")
        void shouldReturnEmptyWhenUsernameNotFound() {
            // given
            // when
            Optional<HumanPrincipalEntity> result = humanPrincipalRepository.findByUsername("nonexistent");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByEmail")
    class FindByEmail {

        @Test
        @DisplayName("should find principal by email")
        void shouldFindPrincipalByEmail() {
            // given
            HumanPrincipalEntity principal = createPrincipal("johndoe", "john@example.com");
            entityManager.persistAndFlush(principal);

            // when
            Optional<HumanPrincipalEntity> result = humanPrincipalRepository.findByEmail("john@example.com");

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("should return empty when email not found")
        void shouldReturnEmptyWhenEmailNotFound() {
            // given
            // when
            Optional<HumanPrincipalEntity> result = humanPrincipalRepository.findByEmail("nonexistent@example.com");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByUsername")
    class ExistsByUsername {

        @Test
        @DisplayName("should return true when username exists")
        void shouldReturnTrueWhenUsernameExists() {
            // given
            HumanPrincipalEntity principal = createPrincipal("existinguser", "existing@example.com");
            entityManager.persistAndFlush(principal);

            // when
            boolean exists = humanPrincipalRepository.existsByUsername("existinguser");

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("should return false when username does not exist")
        void shouldReturnFalseWhenUsernameDoesNotExist() {
            // given
            // when
            boolean exists = humanPrincipalRepository.existsByUsername("nonexistent");

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("existsByEmail")
    class ExistsByEmail {

        @Test
        @DisplayName("should return true when email exists")
        void shouldReturnTrueWhenEmailExists() {
            // given
            HumanPrincipalEntity principal = createPrincipal("user", "existing@example.com");
            entityManager.persistAndFlush(principal);

            // when
            boolean exists = humanPrincipalRepository.existsByEmail("existing@example.com");

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("should return false when email does not exist")
        void shouldReturnFalseWhenEmailDoesNotExist() {
            // given
            // when
            boolean exists = humanPrincipalRepository.existsByEmail("nonexistent@example.com");

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("findInactiveByEmail")
    class FindInactiveByEmail {

        @Test
        @DisplayName("should find inactive principal by email")
        void shouldFindInactivePrincipalByEmail() {
            // given
            HumanPrincipalEntity principal = createPrincipal("inactiveuser", "inactive@example.com");
            principal.setStatus(PrincipalStatus.INACTIVE);
            entityManager.persistAndFlush(principal);

            // when
            Optional<HumanPrincipalEntity> result = humanPrincipalRepository.findInactiveByEmail("inactive@example.com");

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getStatus()).isEqualTo(PrincipalStatus.INACTIVE);
        }

        @Test
        @DisplayName("should return empty when email exists but is not inactive")
        void shouldReturnEmptyWhenEmailExistsButNotInactive() {
            // given
            HumanPrincipalEntity principal = createPrincipal("activeuser", "active@example.com");
            principal.setStatus(PrincipalStatus.ACTIVE);
            entityManager.persistAndFlush(principal);

            // when
            Optional<HumanPrincipalEntity> result = humanPrincipalRepository.findInactiveByEmail("active@example.com");

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty when email does not exist")
        void shouldReturnEmptyWhenEmailNotExists() {
            // given
            // when
            Optional<HumanPrincipalEntity> result = humanPrincipalRepository.findInactiveByEmail("nonexistent@example.com");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByPrincipalId")
    class FindByPrincipalId {

        @Test
        @DisplayName("should find principal by ID")
        void shouldFindPrincipalById() {
            // given
            HumanPrincipalEntity principal = createPrincipal("user", "user@example.com");
            entityManager.persistAndFlush(principal);
            UUID principalId = principal.getBusinessId().value();

            // when
            Optional<HumanPrincipalEntity> result = humanPrincipalRepository.findByBusinessId(principalId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getBusinessId()).isEqualTo(principalId);
        }

        @Test
        @DisplayName("should return empty when ID does not exist")
        void shouldReturnEmptyWhenIdDoesNotExist() {
            // given
            UUID nonExistentId = UUID.randomUUID();

            // when
            Optional<HumanPrincipalEntity> result = humanPrincipalRepository.findByBusinessId(nonExistentId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("searchPrincipals")
    class SearchPrincipals {

        @Test
        @DisplayName("should search principals by username partial match")
        void shouldSearchByUsernamePartialMatch() {
            // given
            entityManager.persistAndFlush(createPrincipal("johndoe", "john@example.com"));
            entityManager.persistAndFlush(createPrincipal("janedoe", "jane@example.com"));
            entityManager.persistAndFlush(createPrincipal("bobsmith", "bob@example.com"));

            // when
            Page<HumanPrincipalEntity> result = humanPrincipalRepository.searchPrincipals(
                    "doe", null, null, PageRequest.of(0, 10));

            // then
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent())
                    .extracting(HumanPrincipalEntity::getUsername)
                    .containsExactlyInAnyOrder("johndoe", "janedoe");
        }

        @Test
        @DisplayName("should search principals by email partial match")
        void shouldSearchByEmailPartialMatch() {
            // given
            entityManager.persistAndFlush(createPrincipal("john", "john@example.com"));
            entityManager.persistAndFlush(createPrincipal("jane", "jane@example.com"));
            entityManager.persistAndFlush(createPrincipal("bob", "bob@acme.com"));

            // when
            Page<HumanPrincipalEntity> result = humanPrincipalRepository.searchPrincipals(
                    "example", null, null, PageRequest.of(0, 10));

            // then
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent())
                    .extracting(HumanPrincipalEntity::getEmail)
                    .containsExactlyInAnyOrder("john@example.com", "jane@example.com");
        }

        @Test
        @DisplayName("should filter principals by status")
        void shouldFilterByStatus() {
            // given
            HumanPrincipalEntity active = createPrincipal("activeuser", "active@example.com");
            active.setStatus(PrincipalStatus.ACTIVE);
            entityManager.persistAndFlush(active);

            HumanPrincipalEntity pending = createPrincipal("pendinguser", "pending@example.com");
            pending.setStatus(PrincipalStatus.PENDING);
            entityManager.persistAndFlush(pending);

            // when
            Page<HumanPrincipalEntity> result = humanPrincipalRepository.searchPrincipals(
                    null, PrincipalStatus.ACTIVE, null, PageRequest.of(0, 10));

            // then
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(PrincipalStatus.ACTIVE);
        }


        @Test
        @DisplayName("should return all principals when no filters")
        void shouldReturnAllWhenNoFilters() {
            // given
            entityManager.persistAndFlush(createPrincipal("user1", "user1@example.com"));
            entityManager.persistAndFlush(createPrincipal("user2", "user2@example.com"));
            entityManager.persistAndFlush(createPrincipal("user3", "user3@example.com"));

            // when
            Page<HumanPrincipalEntity> result = humanPrincipalRepository.searchPrincipals(
                    null, null, null, PageRequest.of(0, 10));

            // then
            assertThat(result.getTotalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("should paginate results correctly")
        void shouldPaginateResultsCorrectly() {
            // given
            for (int i = 1; i <= 25; i++) {
                entityManager.persistAndFlush(createPrincipal("user" + i, "user" + i + "@example.com"));
            }

            // when
            Page<HumanPrincipalEntity> firstPage = humanPrincipalRepository.searchPrincipals(
                    null, null, null, PageRequest.of(0, 10));
            Page<HumanPrincipalEntity> secondPage = humanPrincipalRepository.searchPrincipals(
                    null, null, null, PageRequest.of(1, 10));
            Page<HumanPrincipalEntity> thirdPage = humanPrincipalRepository.searchPrincipals(
                    null, null, null, PageRequest.of(2, 10));

            // then
            assertThat(firstPage.getTotalElements()).isEqualTo(25);
            assertThat(firstPage.getContent()).hasSize(10);
            assertThat(secondPage.getContent()).hasSize(10);
            assertThat(thirdPage.getContent()).hasSize(5);
        }

        @Test
        @DisplayName("should be case insensitive for search")
        void shouldBeCaseInsensitiveForSearch() {
            // given
            entityManager.persistAndFlush(createPrincipal("JohnDoe", "John@Example.COM"));

            // when
            Page<HumanPrincipalEntity> resultLower = humanPrincipalRepository.searchPrincipals(
                    "johndoe", null, null, PageRequest.of(0, 10));
            Page<HumanPrincipalEntity> resultUpper = humanPrincipalRepository.searchPrincipals(
                    "JOHNDOE", null, null, PageRequest.of(0, 10));
            Page<HumanPrincipalEntity> resultMixed = humanPrincipalRepository.searchPrincipals(
                    "JoHnDoE", null, null, PageRequest.of(0, 10));

            // then
            assertThat(resultLower.getTotalElements()).isEqualTo(1);
            assertThat(resultUpper.getTotalElements()).isEqualTo(1);
            assertThat(resultMixed.getTotalElements()).isEqualTo(1);
        }
    }

    // Helper methods

    private HumanPrincipalEntity createPrincipal(String username, String email) {
        HumanPrincipalEntity entity = new HumanPrincipalEntity();
        entity.setUsername(username);
        entity.setEmail(email);
        entity.setFirstName("Test");
        entity.setLastName("User");
        entity.setDisplayName("Test User");
        entity.setStatus(PrincipalStatus.PENDING);
        entity.setSyncStatus(SyncStatus.PENDING);
        return entity;
    }
}
