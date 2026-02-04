package io.openleap.iam.principal.repository;

import io.openleap.iam.principal.domain.entity.HumanPrincipalEntity;
import io.openleap.iam.principal.domain.entity.PrincipalId;
import io.openleap.iam.principal.domain.entity.ServicePrincipalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServicePrincipalRepository extends JpaRepository<ServicePrincipalEntity, UUID> {
    default Optional<ServicePrincipalEntity> findByBusinessId(PrincipalId businessId) {
        return findByBusinessId(businessId.value());
    }

    @Query("SELECT p FROM ServicePrincipalEntity p WHERE p.businessId.value = :businessId")
    Optional<ServicePrincipalEntity> findByBusinessId(@Param("businessId") UUID businessId);

    /**
     * Find by username (globally unique)
     */
    Optional<ServicePrincipalEntity> findByUsername(String username);
    
    /**
     * Check if username exists (for validation)
     */
    boolean existsByUsername(String username);
    
    /**
     * Find by service name (globally unique)
     */
    Optional<ServicePrincipalEntity> findByServiceName(String serviceName);
    
    /**
     * Check if service name exists (for validation)
     */
    boolean existsByServiceName(String serviceName);
}
