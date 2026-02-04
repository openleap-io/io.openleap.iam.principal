package io.openleap.iam.principal.repository;

import io.openleap.iam.principal.domain.entity.DevicePrincipalEntity;
import io.openleap.iam.principal.domain.entity.PrincipalId;
import io.openleap.iam.principal.domain.entity.SystemPrincipalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DevicePrincipalRepository extends JpaRepository<DevicePrincipalEntity, UUID> {
    default Optional<DevicePrincipalEntity> findByBusinessId(PrincipalId businessId) {
        return findByBusinessId(businessId.value());
    }

    @Query("SELECT p FROM DevicePrincipalEntity p WHERE p.businessId.value = :businessId")
    Optional<DevicePrincipalEntity> findByBusinessId(@Param("businessId") UUID businessId);

    /**
     * Find by username (globally unique)
     */
    Optional<DevicePrincipalEntity> findByUsername(String username);
    
    /**
     * Check if username exists (for validation)
     */
    boolean existsByUsername(String username);
    
    /**
     * Find by device identifier (globally unique)
     */
    Optional<DevicePrincipalEntity> findByDeviceIdentifier(String deviceIdentifier);
    
    /**
     * Check if device identifier exists (for validation)
     */
    boolean existsByDeviceIdentifier(String deviceIdentifier);
}
