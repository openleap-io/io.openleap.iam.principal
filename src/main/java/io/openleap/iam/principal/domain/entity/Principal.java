package io.openleap.iam.principal.domain.entity;

import io.openleap.common.domain.DomainEntity;
import io.openleap.common.persistence.entity.AuditableEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@MappedSuperclass
public abstract class Principal extends AuditableEntity implements DomainEntity<PrincipalId> {
    @Embedded
    private PrincipalId businessId;
    /**
     * Login username (globally unique across all principal tables, max 100 chars, lowercase, immutable)
     */
    @Column(name = "username", nullable = false, unique = true, length = 100, updatable = false)
    private String username;
    
    /**
     * Email address (globally unique if provided, nullable for SERVICE/SYSTEM/DEVICE)
     */
    @Column(name = "email", length = 255)
    private String email;
    
    /**
     * Primary tenant (FK to iam_tenant.tenants)
     */
    @Column(name = "default_tenant_id", nullable = false, updatable = false)
    private UUID defaultTenantId;
    
    /**
     * Account state
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private PrincipalStatus status;
    
    /**
     * Optional business hints (lightweight classification only, max 10KB)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context_tags", columnDefinition = "jsonb")
    private Map<String, Object> contextTags;
    
    /**
     * Keycloak sync state
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false, length = 50)
    private SyncStatus syncStatus = SyncStatus.PENDING;
    
    /**
     * Sync retry count
     */
    @Column(name = "sync_retry_count", nullable = false)
    private Integer syncRetryCount = 0;

    @PrePersist
    protected void onCreate() {
        if (getCreatedAt() == null) {
            setCreatedAt(Instant.now());
        }
        if (getUpdatedAt() == null) {
            setUpdatedAt(Instant.now());
        }
        if (syncStatus == null) {
            syncStatus = SyncStatus.PENDING;
        }
        if (syncRetryCount == null) {
            syncRetryCount = 0;
        }
    }
    
    // Getters and Setters


    public UUID getDefaultTenantId() {
        return defaultTenantId;
    }

    public void setDefaultTenantId(UUID defaultTenantId) {
        this.defaultTenantId = defaultTenantId;
    }

    @Override
    public PrincipalId getBusinessId() {
        return businessId;
    }

    public void setBusinessId(PrincipalId businessId) {
        this.businessId = businessId;
    }
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public PrincipalStatus getStatus() {
        return status;
    }
    
    public void setStatus(PrincipalStatus status) {
        this.status = status;
    }
    
    public Map<String, Object> getContextTags() {
        return contextTags;
    }
    
    public void setContextTags(Map<String, Object> contextTags) {
        this.contextTags = contextTags;
    }
    
    public SyncStatus getSyncStatus() {
        return syncStatus;
    }
    
    public void setSyncStatus(SyncStatus syncStatus) {
        this.syncStatus = syncStatus;
    }
    
    public Integer getSyncRetryCount() {
        return syncRetryCount;
    }
    
    public void setSyncRetryCount(Integer syncRetryCount) {
        this.syncRetryCount = syncRetryCount;
    }
    
    /**
     * Get the principal type (discriminator).
     * 
     * @return the principal type
     */
    public abstract PrincipalType getPrincipalType();
}

