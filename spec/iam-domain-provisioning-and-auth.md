# IAM core concepts

<!-- TOC -->

* [IAM core concepts](#iam-core-concepts)
    * [Roles and responsibilities](#roles-and-responsibilities)
    * [Tenant creation and provisioning](#tenant-creation-and-provisioning)
    * [Principal creation and provisioning](#principal-creation-and-provisioning)
    * [Authentication and authorization flow](#authentication-and-authorization-flow)

<!-- TOC -->

## Roles and responsibilities

General domain and conceptual rules:

1. Authentication and Authorization are separate concerns!
2. `iam.principal` is the core domain representing an identity in the system used for authentication.
3. `iam.authz` is the core domain representing authorization policies and roles used for authorization.
4. `iam.tenant`  is the core domain representing a tenant (organization) in the system.
5. `iam.audit` logs all significant actions for compliance and monitoring.

Decoupling domain rules:

1. principal can be created without any authorization roles assigned, without any tenant assigned.
2. tenant can be created without any principals assigned.
3. authorization roles can be created without any principals assigned.
4. authorization roles can be created without any tenants assigned.

## Tenant creation and provisioning

On the tenant domain we have a `default` tenant that represents and holds all principals that does not provide the
default tenant on principal creation. When creating a principal there is `defaultTenantId` and it is optional. When a
new
tenant is created, the following sequence of events occurs:

```mermaid
sequenceDiagram
    actor Admin as Platform Admin
    participant Tenant as iam.tenant
    participant KC as Keycloak
    participant User as iam.user
    participant AuthZ as iam.authz
    participant DB as Database
    Admin ->> Tenant: POST /tenants (create)
    Tenant ->> Tenant: Validate & Create Tenant<br/>(status=PROVISIONING)
    Tenant ->> DB: Create Tenant Schema/Namespace
    Tenant ->> KC: Create Keycloak Realm/Org
    KC -->> Tenant: Realm Created
    Tenant ->> Tenant: Create Default Organization
    Tenant ->> Tenant: Create TenantConfig
    Tenant ->> Tenant: Create TenantQuotas
    Tenant ->> Tenant: Update status=ACTIVE
    Tenant ->> User: Publish iam.tenant.created
    User ->> User: Create Default Admin User
    Tenant -->> Admin: Tenant Created
```

## Principal creation and provisioning

When a new principal is created, the following sequence of events occurs:

```mermaid
sequenceDiagram
    participant EXT as External System<br/>(HR, Portal, API)
    participant PRINCIPAL as iam.principal
    participant KC as Keycloak
    participant TENANT as iam.tenant
    participant AUTHZ as iam.authz
    participant AUDIT as iam.audit
    EXT ->> PRINCIPAL: Create Principal Request
    PRINCIPAL ->> PRINCIPAL: Validate Data
    alt check if defaultTenantId provided
        opt defaultTenantId not provided
            PRINCIPAL ->> TENANT: Get System default Tenant
            TENANT ->> PRINCIPAL: System default Tenant
        end
    else
        opt validate request provided defaultTenantId for a principal
            PRINCIPAL ->> TENANT: Verify Tenant Exists
            TENANT ->> PRINCIPAL: Tenant Valid
        end
    end
    PRINCIPAL ->> KC: Create User (Admin API)
    KC ->> PRINCIPAL: User Created (Keycloak ID)
    PRINCIPAL ->> PRINCIPAL: Link Keycloak ID
    PRINCIPAL ->> PRINCIPAL: Set Status=ACTIVE
    PRINCIPAL ->> AUTHZ: Publish: principal.created
    AUTHZ ->> AUTHZ: Assign Default Roles (if applicable)
    PRINCIPAL ->> AUDIT: Publish: principal.provisioned
    PRINCIPAL ->> EXT: Principal Created Successfully
```

The idea behind `defaultTenantId` making it optional is to provide a more convenient way to onboard new principals into
the system without
requiring the tenant information upfront.

The `defaultTenantId` is only part of this domain again to provide more convenience during principal creation, so that
it is not required to make another call to assign a tenant after the principal is created.

## Authentication and authorization flow

User wants to access some protected resource in the Application. The Application is configured to use Keycloak as
the Authorization Server (AS). The principals are synced with Keycloak. The following sequence diagram illustrates the
authentication and authorization flow:

```mermaid
sequenceDiagram
    actor U
    participant G as Application
    participant AS as Keycloak
    participant AuthN as iam.principal
    participant T as iam.tenant
    participant AuthZ as iam.authz
    note over AS, AuthN: Principals are synced with Keycloak
    U ->> G: Request Protected Resource
    activate G
    G -->> U: 302 Redirect to Authorization Server
    deactivate G
    activate AS
    U ->> AS: 2. GET /authorize?...
    AS -->> U: 302 Redirect to Login
    U ->> AS: 4. Authenticate
    AS -->> U: 302 Authorization Code
    deactivate AS
    activate G
    U ->> G: 5. GET /callback with code
    activate AS
    G ->> AS: POST /token + code
    AS -->> G: 200 id_token + access_token
    deactivate AS
    G -->> U: 302 Redirect to Request Protected Resource
    U ->> G: 6. Request Protected Resource
    note over G: At this point the user is only authenticated, <br/> not yet authorized to access the resource, <br/> so the Application needs to check authorization.
    note over G: JWT token contains authentication claims <br/> + roles only if it is admin user
    opt list of tenants for selection
        note over G: Optional: Application calls for user tenant info <br/> if needed for authorization decision
        G ->> T: GET /tenant/info + token
        T -->> G: Tenant Info, user tenant memberships
        note over G: Application/User selects tenant for <br/> the session if user has multiple tenant memberships
    end
    note over G: If tenant is not provided the system uses the default tenant for authorization check
    G ->> AuthZ: POST /authz/check + token + resource + action + tenant (if applicable)
```