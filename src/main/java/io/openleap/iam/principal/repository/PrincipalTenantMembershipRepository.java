package io.openleap.iam.principal.repository;

import io.openleap.iam.principal.domain.entity.MembershipStatus;
import io.openleap.iam.principal.domain.entity.PrincipalTenantMembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
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

    /**
     * Finds an active membership for a given principal and tenant.
     *
     * @param principalId the principal ID
     * @param tenantId the tenant ID
     * @param status the membership status
     * @return the membership if found
     */
    Optional<PrincipalTenantMembershipEntity> findByPrincipalIdAndTenantIdAndStatus(
            UUID principalId, UUID tenantId, MembershipStatus status);

    /**
     * Finds a membership for a given principal and tenant.
     *
     * @param principalId the principal ID
     * @param tenantId the tenant ID
     * @return the membership if found
     */
    Optional<PrincipalTenantMembershipEntity> findByPrincipalIdAndTenantId(UUID principalId, UUID tenantId);
}

