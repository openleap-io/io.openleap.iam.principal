CREATE SCHEMA IF NOT EXISTS iam_principal;

CREATE TABLE iam_principal.human_principals (
    -- Inherited attributes from abstract Principal
    principal_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,  -- Required for humans
    primary_tenant_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'INACTIVE', 'DELETED')),
    context_tags JSONB,
    sync_status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (sync_status IN ('PENDING', 'SYNCING', 'SYNCED', 'FAILED')),
    sync_retry_count INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- Human-specific attributes (includes profile data)
    keycloak_user_id VARCHAR(255) UNIQUE,
    email_verified BOOLEAN DEFAULT FALSE,
    mfa_enabled BOOLEAN DEFAULT FALSE,
    last_login_at TIMESTAMP,
    display_name VARCHAR(200) NOT NULL,
    phone VARCHAR(20),
    language VARCHAR(10),
    timezone VARCHAR(100),
    locale VARCHAR(20),
    avatar_url VARCHAR(500),
    bio TEXT,
    preferences JSONB,
    
    -- Constraints
    -- Note: Foreign key to iam_tenant.tenants is not created here as it's a cross-service reference.
    -- Referential integrity is enforced at the application layer.
    CONSTRAINT chk_human_context_tags_size CHECK (pg_column_size(context_tags) <= 10240)
);

CREATE INDEX idx_human_principals_status ON iam_principal.human_principals(status);
CREATE INDEX idx_human_principals_tenant ON iam_principal.human_principals(primary_tenant_id);
CREATE INDEX idx_human_principals_sync_status ON iam_principal.human_principals(sync_status) WHERE sync_status != 'SYNCED';
CREATE INDEX idx_human_principals_context_tags ON iam_principal.human_principals USING GIN (context_tags);
CREATE INDEX idx_human_principals_language ON iam_principal.human_principals(language);
CREATE INDEX idx_human_principals_email ON iam_principal.human_principals(email);


CREATE TABLE iam_principal.service_principals (
    -- Inherited attributes from abstract Principal
    principal_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255),  -- Optional for services
    primary_tenant_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'INACTIVE', 'DELETED')),
    context_tags JSONB,
    sync_status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (sync_status IN ('PENDING', 'SYNCING', 'SYNCED', 'FAILED')),
    sync_retry_count INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- Service-specific attributes
    keycloak_client_id VARCHAR(255) UNIQUE,
    service_name VARCHAR(200) UNIQUE NOT NULL,
    allowed_scopes TEXT[],
    api_key_hash VARCHAR(64) NOT NULL,
    credential_rotation_date DATE NOT NULL,
    rotated_at TIMESTAMP,
    
    -- Constraints
    -- Note: Foreign key to iam_tenant.tenants is not created here as it's a cross-service reference.
    -- Referential integrity is enforced at the application layer.
    CONSTRAINT chk_service_context_tags_size CHECK (pg_column_size(context_tags) <= 10240),
    CONSTRAINT chk_service_rotation_date_future CHECK (credential_rotation_date >= CURRENT_DATE)
);

CREATE INDEX idx_service_principals_status ON iam_principal.service_principals(status);
CREATE INDEX idx_service_principals_tenant ON iam_principal.service_principals(primary_tenant_id);
CREATE INDEX idx_service_principals_sync_status ON iam_principal.service_principals(sync_status) WHERE sync_status != 'SYNCED';
CREATE INDEX idx_service_principals_context_tags ON iam_principal.service_principals USING GIN (context_tags);
-- Index for credential rotation queries (application layer filters by date)
CREATE INDEX idx_service_principals_rotation ON iam_principal.service_principals(credential_rotation_date);


CREATE TABLE iam_principal.system_principals (
    -- Inherited attributes from abstract Principal
    principal_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255),  -- Optional for systems
    primary_tenant_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'INACTIVE', 'DELETED')),
    context_tags JSONB,
    sync_status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (sync_status IN ('PENDING', 'SYNCING', 'SYNCED', 'FAILED')),
    sync_retry_count INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- System-specific attributes
    keycloak_client_id VARCHAR(255) UNIQUE,
    system_identifier VARCHAR(200) UNIQUE NOT NULL,
    integration_type VARCHAR(100) CHECK (integration_type IN ('ERP', 'CRM', 'EXTERNAL_API', 'PARTNER')),
    certificate_thumbprint VARCHAR(64),
    allowed_operations TEXT[],
    
    -- Constraints
    -- Note: Foreign key to iam_tenant.tenants is not created here as it's a cross-service reference.
    -- Referential integrity is enforced at the application layer.
    CONSTRAINT chk_system_context_tags_size CHECK (pg_column_size(context_tags) <= 10240)
);

CREATE INDEX idx_system_principals_status ON iam_principal.system_principals(status);
CREATE INDEX idx_system_principals_tenant ON iam_principal.system_principals(primary_tenant_id);
CREATE INDEX idx_system_principals_sync_status ON iam_principal.system_principals(sync_status) WHERE sync_status != 'SYNCED';
CREATE INDEX idx_system_principals_context_tags ON iam_principal.system_principals USING GIN (context_tags);
CREATE INDEX idx_system_principals_integration_type ON iam_principal.system_principals(integration_type);


CREATE TABLE iam_principal.device_principals (
    -- Inherited attributes from abstract Principal
    principal_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255),  -- Optional for devices
    primary_tenant_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'INACTIVE', 'DELETED')),
    context_tags JSONB,
    sync_status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (sync_status IN ('PENDING', 'SYNCING', 'SYNCED', 'FAILED')),
    sync_retry_count INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- Device-specific attributes
    keycloak_client_id VARCHAR(255) UNIQUE,
    device_identifier VARCHAR(200) UNIQUE NOT NULL,
    device_type VARCHAR(50) NOT NULL CHECK (device_type IN ('IOT_SENSOR', 'EDGE_DEVICE', 'KIOSK', 'TERMINAL', 'GATEWAY', 'OTHER')),
    manufacturer VARCHAR(100),
    model VARCHAR(100),
    firmware_version VARCHAR(50),
    certificate_thumbprint VARCHAR(64),
    last_heartbeat_at TIMESTAMP,
    location_info JSONB,
    
    -- Constraints
    -- Note: Foreign key to iam_tenant.tenants is not created here as it's a cross-service reference.
    -- Referential integrity is enforced at the application layer.
    CONSTRAINT chk_device_context_tags_size CHECK (pg_column_size(context_tags) <= 10240),
    CONSTRAINT chk_device_location_info_size CHECK (pg_column_size(location_info) <= 5120)
);

CREATE INDEX idx_device_principals_status ON iam_principal.device_principals(status);
CREATE INDEX idx_device_principals_tenant ON iam_principal.device_principals(primary_tenant_id);
CREATE INDEX idx_device_principals_sync_status ON iam_principal.device_principals(sync_status) WHERE sync_status != 'SYNCED';
CREATE INDEX idx_device_principals_context_tags ON iam_principal.device_principals USING GIN (context_tags);
CREATE INDEX idx_device_principals_device_type ON iam_principal.device_principals(device_type);
CREATE INDEX idx_device_principals_device_identifier ON iam_principal.device_principals(device_identifier);


CREATE TABLE iam_principal.principal_tenant_memberships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    principal_id UUID NOT NULL,
    principal_type VARCHAR(50) NOT NULL CHECK (principal_type IN ('HUMAN', 'SERVICE', 'SYSTEM', 'DEVICE')),
    tenant_id UUID NOT NULL,
    valid_from DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'SUSPENDED', 'EXPIRED')),
    invited_by UUID,
    invited_by_type VARCHAR(50) CHECK (invited_by_type IN ('HUMAN', 'SERVICE', 'SYSTEM', 'DEVICE')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- Note: No FK to principal tables because we use composite reference pattern
    -- Application layer enforces referential integrity via principal_type discriminator
    -- Note: Foreign key to iam_tenant.tenants is not created here as it's a cross-service reference.
    -- Referential integrity is enforced at the application layer.
    CONSTRAINT chk_valid_period CHECK (valid_to IS NULL OR valid_from <= valid_to),
    CONSTRAINT uk_principal_tenant_active UNIQUE (principal_id, principal_type, tenant_id, status)
);

CREATE INDEX idx_memberships_principal ON iam_principal.principal_tenant_memberships(principal_id, principal_type);
CREATE INDEX idx_memberships_tenant ON iam_principal.principal_tenant_memberships(tenant_id);
CREATE INDEX idx_memberships_status ON iam_principal.principal_tenant_memberships(status);
CREATE INDEX idx_memberships_valid_to ON iam_principal.principal_tenant_memberships(valid_to) WHERE valid_to IS NOT NULL;

-- View to check global username uniqueness across all principal tables
CREATE OR REPLACE VIEW iam_principal.all_principals_usernames AS
SELECT principal_id, username, 'HUMAN' as principal_type FROM iam_principal.human_principals
UNION ALL
SELECT principal_id, username, 'SERVICE' as principal_type FROM iam_principal.service_principals
UNION ALL
SELECT principal_id, username, 'SYSTEM' as principal_type FROM iam_principal.system_principals
UNION ALL
SELECT principal_id, username, 'DEVICE' as principal_type FROM iam_principal.device_principals;


CREATE OR REPLACE FUNCTION iam_principal.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_human_principals_updated_at
    BEFORE UPDATE ON iam_principal.human_principals
    FOR EACH ROW
    EXECUTE FUNCTION iam_principal.update_updated_at_column();

CREATE TRIGGER trigger_service_principals_updated_at
    BEFORE UPDATE ON iam_principal.service_principals
    FOR EACH ROW
    EXECUTE FUNCTION iam_principal.update_updated_at_column();

CREATE TRIGGER trigger_system_principals_updated_at
    BEFORE UPDATE ON iam_principal.system_principals
    FOR EACH ROW
    EXECUTE FUNCTION iam_principal.update_updated_at_column();

CREATE TRIGGER trigger_device_principals_updated_at
    BEFORE UPDATE ON iam_principal.device_principals
    FOR EACH ROW
    EXECUTE FUNCTION iam_principal.update_updated_at_column();

CREATE TABLE IF NOT EXISTS outbox (
    pk BIGSERIAL PRIMARY KEY,
    uuid UUID,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    created_by TEXT,
    updated_at TIMESTAMP,
    updated_by TEXT,
    exchange_key TEXT NOT NULL,
    routing_key VARCHAR(256) NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    published BOOLEAN NOT NULL,
    attempts INT NOT NULL,
    next_attempt_at TIMESTAMP,
    last_error VARCHAR(4000),
    payload_json TEXT NOT NULL,
    headers_json TEXT
);
