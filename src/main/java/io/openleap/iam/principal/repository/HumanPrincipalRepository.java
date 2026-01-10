package io.openleap.iam.principal.repository;

import io.openleap.iam.principal.domain.entity.HumanPrincipalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HumanPrincipalRepository extends JpaRepository<HumanPrincipalEntity, UUID> {
    
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
}

