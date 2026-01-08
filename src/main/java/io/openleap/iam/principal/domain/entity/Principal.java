package io.openleap.iam.principal.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@MappedSuperclass
public abstract class Principal {
    
    /**
     * Unique identifier (PK, immutable, generated)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "principal_id", nullable = false, updatable = false)
    private UUID principalId;
    
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
    @Column(name = "primary_tenant_id", nullable = false, updatable = false)
    private UUID primaryTenantId;
    
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
    
    /**
     * Creation time (auto-generated)
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    /**
     * Creator principal ID (nullable)
     */
    @Column(name = "created_by")
    private UUID createdBy;
    
    /**
     * Last update time (auto-updated)
     */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
        if (syncStatus == null) {
            syncStatus = SyncStatus.PENDING;
        }
        if (syncRetryCount == null) {
            syncRetryCount = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
    
    // Getters and Setters
    
    public UUID getPrincipalId() {
        return principalId;
    }
    
    public void setPrincipalId(UUID principalId) {
        this.principalId = principalId;
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
    
    public UUID getPrimaryTenantId() {
        return primaryTenantId;
    }
    
    public void setPrimaryTenantId(UUID primaryTenantId) {
        this.primaryTenantId = primaryTenantId;
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
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public UUID getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Get the principal type (discriminator).
     * 
     * @return the principal type
     */
    public abstract PrincipalType getPrincipalType();
}

