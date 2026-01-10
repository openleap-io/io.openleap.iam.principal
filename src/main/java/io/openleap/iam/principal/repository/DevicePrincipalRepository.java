package io.openleap.iam.principal.repository;

import io.openleap.iam.principal.domain.entity.DevicePrincipalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DevicePrincipalRepository extends JpaRepository<DevicePrincipalEntity, UUID> {
    
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
