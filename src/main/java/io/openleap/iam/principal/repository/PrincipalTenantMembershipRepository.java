package io.openleap.iam.principal.repository;

import io.openleap.iam.principal.domain.entity.PrincipalTenantMembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PrincipalTenantMembershipRepository extends JpaRepository<PrincipalTenantMembershipEntity, UUID> {
    
    /**
     * Finds all memberships for a given principal ID.
     * 
     * @param principalId the principal ID
     * @return list of memberships
     */
    List<PrincipalTenantMembershipEntity> findByPrincipalId(UUID principalId);
}

