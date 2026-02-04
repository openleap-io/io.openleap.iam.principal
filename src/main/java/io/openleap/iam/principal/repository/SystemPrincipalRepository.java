package io.openleap.iam.principal.repository;

import io.openleap.iam.principal.domain.entity.PrincipalId;
import io.openleap.iam.principal.domain.entity.SystemPrincipalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SystemPrincipalRepository extends JpaRepository<SystemPrincipalEntity, UUID> {
    default Optional<SystemPrincipalEntity> findByBusinessId(PrincipalId businessId) {
        return findByBusinessId(businessId.value());
    }

    @Query("SELECT p FROM SystemPrincipalEntity p WHERE p.businessId.value = :businessId")
    Optional<SystemPrincipalEntity> findByBusinessId(@Param("businessId") UUID businessId);

    /**
     * Find by username (globally unique)
     */
    Optional<SystemPrincipalEntity> findByUsername(String username);

    /**
     * Check if username exists (for validation)
     */
    boolean existsByUsername(String username);

    /**
     * Find by system identifier (globally unique)
     */
    Optional<SystemPrincipalEntity> findBySystemIdentifier(String systemIdentifier);

    /**
     * Check if system identifier exists (for validation)
     */
    boolean existsBySystemIdentifier(String systemIdentifier);
}
