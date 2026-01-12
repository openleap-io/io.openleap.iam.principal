package io.openleap.iam.principal.repository;

import io.openleap.iam.principal.domain.entity.HumanPrincipalEntity;
import io.openleap.iam.principal.domain.entity.PrincipalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HumanPrincipalRepository extends JpaRepository<HumanPrincipalEntity, UUID>, JpaSpecificationExecutor<HumanPrincipalEntity> {
    
    /**
     * Find by username (globally unique)
     */
    Optional<HumanPrincipalEntity> findByUsername(String username);
    
    /**
     * Find by email (globally unique if provided)
     */
    Optional<HumanPrincipalEntity> findByEmail(String email);
    
    /**
     * Check if username exists (for validation)
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists (for validation)
     */
    boolean existsByEmail(String email);
    
    /**
     * Find inactive principal by email (for reactivation flow)
     */
    @Query("SELECT h FROM HumanPrincipalEntity h WHERE h.email = :email AND h.status = 'INACTIVE'")
    Optional<HumanPrincipalEntity> findInactiveByEmail(String email);

    Optional<HumanPrincipalEntity> findByPrincipalId(UUID principalId);

    /**
     * Search principals with filters
     */
    @Query("SELECT h FROM HumanPrincipalEntity h WHERE " +
           "(:search IS NULL OR LOWER(h.username) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(h.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR h.status = :status) AND " +
           "(:tenantId IS NULL OR h.primaryTenantId = :tenantId)")
    Page<HumanPrincipalEntity> searchPrincipals(String search, PrincipalStatus status, UUID tenantId, Pageable pageable);
}

