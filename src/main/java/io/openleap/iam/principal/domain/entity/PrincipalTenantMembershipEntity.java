package io.openleap.iam.principal.domain.entity;

import io.openleap.common.persistence.entity.AuditableEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

//TODO: To be moved to iam.tenant service
//@Entity
//@Table(name = "principal_tenant_memberships", schema = "iam_principal")
public class PrincipalTenantMembershipEntity {
    
    /**
     * Principal reference (references any principal table)
     */
    @Column(name = "principal_id", nullable = false, updatable = false)
    private UUID principalId;
    
    /**
     * Discriminator indicating which table (HUMAN, SERVICE, SYSTEM, DEVICE)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "principal_type", nullable = false, length = 50, updatable = false)
    private PrincipalType principalType;
    
    /**
     * Tenant reference (FK to iam_tenant.tenants)
     */
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;
    
    /**
     * Membership start date (required, default CURRENT_DATE)
     */
    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;
    
    /**
     * Membership end date (optional, null = no expiry)
     */
    @Column(name = "valid_to")
    private LocalDate validTo;
    
    /**
     * Membership state
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private MembershipStatus status = MembershipStatus.ACTIVE;
    
    /**
     * Inviting principal ID (nullable)
     */
    @Column(name = "invited_by")
    private UUID invitedBy;
    
    /**
     * Inviting principal type (nullable)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "invited_by_type", length = 50)
    private PrincipalType invitedByType;

    @PrePersist
    protected void onCreate() {
//        if (getCreatedAt() == null) {
//            setCreatedAt(Instant.now());
//        }
        if (status == null) {
            status = MembershipStatus.ACTIVE;
        }
        if (validFrom == null) {
            validFrom = LocalDate.now();
        }
    }
    
    // Getters and Setters

    
    public UUID getPrincipalId() {
        return principalId;
    }
    
    public void setPrincipalId(UUID principalId) {
        this.principalId = principalId;
    }
    
    public PrincipalType getPrincipalType() {
        return principalType;
    }
    
    public void setPrincipalType(PrincipalType principalType) {
        this.principalType = principalType;
    }
    
    public UUID getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }
    
    public LocalDate getValidFrom() {
        return validFrom;
    }
    
    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }
    
    public LocalDate getValidTo() {
        return validTo;
    }
    
    public void setValidTo(LocalDate validTo) {
        this.validTo = validTo;
    }
    
    public MembershipStatus getStatus() {
        return status;
    }
    
    public void setStatus(MembershipStatus status) {
        this.status = status;
    }
    
    public UUID getInvitedBy() {
        return invitedBy;
    }
    
    public void setInvitedBy(UUID invitedBy) {
        this.invitedBy = invitedBy;
    }
    
    public PrincipalType getInvitedByType() {
        return invitedByType;
    }
    
    public void setInvitedByType(PrincipalType invitedByType) {
        this.invitedByType = invitedByType;
    }

}

