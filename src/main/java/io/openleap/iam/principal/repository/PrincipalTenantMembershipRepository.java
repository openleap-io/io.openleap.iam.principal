package io.openleap.iam.principal.repository;

import io.openleap.iam.principal.domain.entity.PrincipalTenantMembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PrincipalTenantMembershipRepository extends JpaRepository<PrincipalTenantMembershipEntity, UUID> {
}

