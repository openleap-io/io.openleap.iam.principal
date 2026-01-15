# IAM Principal Service

A comprehensive Identity and Access Management (IAM) service for managing principals (users, services, systems, and devices) in a multi-tenant environment. Built with Spring Boot 3.x, Spring Cloud 2025.0.0, and Java 25.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
  - [Local Development](#local-development)
  - [Docker Deployment](#docker-deployment)
  - [Configuration Options](#configuration-options)
- [API Reference](#api-reference)
  - [Human Principals](#human-principals)
  - [Service Principals](#service-principals)
  - [System Principals](#system-principals)
  - [Device Principals](#device-principals)
  - [Admin Operations](#admin-operations)
- [Principal Types](#principal-types)
- [Principal Lifecycle](#principal-lifecycle)
- [Database Schema](#database-schema)
- [Testing](#testing)
- [Environment Variables](#environment-variables)
- [Spring Profiles](#spring-profiles)

---

## Overview

The IAM Principal Service is a microservice responsible for managing identity principals across a multi-tenant platform. It supports four distinct principal types, each with specialized attributes and behaviors:

- **Human Principals**: End users with profiles, preferences, and Keycloak integration
- **Service Principals**: Internal microservices with API keys and OAuth2 client credentials
- **System Principals**: External integrations (ERP, CRM, etc.) with certificate-based authentication
- **Device Principals**: IoT devices, sensors, and edge computing nodes

## Features

- **Multi-tenant Architecture**: Principals can belong to multiple tenants with configurable memberships
- **Keycloak Integration**: Automatic synchronization with Keycloak for OAuth2/OIDC authentication
- **State Machine**: Well-defined principal lifecycle (PENDING → ACTIVE → SUSPENDED → INACTIVE → DELETED)
- **GDPR Compliance**: Built-in support for right to erasure with audit trails
- **Credential Management**: Automatic rotation policies for service principal credentials
- **Device Heartbeat**: Health monitoring for IoT and device principals
- **Cross-tenant Search**: Administrative search across all tenants
- **Profile Management**: Rich profile attributes for human principals

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        API Gateway                               │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                   IAM Principal Service                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │   Human     │  │   Service   │  │   System    │              │
│  │ Controller  │  │ Controller  │  │ Controller  │              │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
│  ┌─────────────┐  ┌─────────────┐                               │
│  │   Device    │  │    Admin    │                               │
│  │ Controller  │  │ Controller  │                               │
│  └─────────────┘  └─────────────┘                               │
│         │                │                                       │
│         ▼                ▼                                       │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                    Service Layer                             ││
│  │  HumanPrincipalService | ServicePrincipalService            ││
│  │  SystemPrincipalService | DevicePrincipalService            ││
│  │  PrincipalService (shared operations)                       ││
│  └─────────────────────────────────────────────────────────────┘│
│         │                │                                       │
│         ▼                ▼                                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │ PostgreSQL  │  │  Keycloak   │  │   Events    │              │
│  │  Database   │  │  (OAuth2)   │  │  (RabbitMQ) │              │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
└─────────────────────────────────────────────────────────────────┘
```

## Prerequisites

- **Java 25** or later
- **Maven 3.8+**
- **PostgreSQL 16+**
- **Keycloak 26+** (for OAuth2/OIDC)
- **Docker & Docker Compose** (optional, for containerized deployment)

## Getting Started
## Local Development

### Prerequisites

- Docker and Docker Compose installed on your machine.
- Java 17 or higher installed.
- Maven installed.

### Building the Service

1. Clone the repository:
```bash
git clone git@github.com:openleap-io/io.openleap.iam.principal.git
```
2. Navigate to the project directory and build the project using Maven:
```bash
mvn clean install
```

### Running the Service Locally
There are two ways to run the service locally, with enabled security and Keycloak, or without security for easier testing.

#### Option 1: With Security and Keycloak
1. Start the Keycloak server and spring cloud necessary services using Docker Compose:
   Navigate to the directory `docker` and run
```bash
docker compose docker-compose.yml up -d
```

2. Run the Spring Boot application:

If you are using IntelliJ IDEA, you can use the `web` configuration or run:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=keycloak,keycloak.web
```
#### Option 2: Without Security and Keycloak
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=logger
```
---

## API Reference

Base URL: `/api/v1/iam/principals`

### Human Principals

#### Create Human Principal
```http
POST /api/v1/iam/principals
Content-Type: application/json
Authorization: Bearer <token>

{
  "username": "john.doe",
  "email": "john.doe@example.com",
  "primary_tenant_id": "550e8400-e29b-41d4-a716-446655440000",
  "display_name": "John Doe",
  "first_name": "John",
  "last_name": "Doe",
  "phone": "+1234567890",
  "language": "en",
  "timezone": "America/New_York",
  "locale": "en-US",
  "preferences": {
    "theme": "dark",
    "notifications": true
  }
}
```

**Response (201 Created):**
```json
{
  "principal_id": "123e4567-e89b-12d3-a456-426614174000",
  "username": "john.doe",
  "email": "john.doe@example.com",
  "status": "ACTIVE",
  "sync_status": "SYNCED",
  "keycloak_user_id": "kc-user-12345"
}
```

#### Get Principal Details
```http
GET /api/v1/iam/principals/{principalId}
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "principal_id": "123e4567-e89b-12d3-a456-426614174000",
  "username": "john.doe",
  "email": "john.doe@example.com",
  "principal_type": "HUMAN",
  "status": "ACTIVE",
  "primary_tenant_id": "550e8400-e29b-41d4-a716-446655440000",
  "created_at": "2024-01-15T10:30:00Z",
  "updated_at": "2024-01-15T10:30:00Z"
}
```

#### Update Profile
```http
PATCH /api/v1/iam/principals/{principalId}/profile
Content-Type: application/json
Authorization: Bearer <token>

{
  "first_name": "Jonathan",
  "display_name": "Johnny Doe",
  "timezone": "Europe/London",
  "preferences": {
    "theme": "light"
  }
}
```

#### Get Profile
```http
GET /api/v1/iam/principals/{principalId}/profile
Authorization: Bearer <token>
```

#### Search Principals
```http
GET /api/v1/iam/principals?search=john&principal_type=HUMAN&status=ACTIVE&tenant_id={tenantId}&page=1&size=50
Authorization: Bearer <token>
```

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `search` | string | Partial match on username or email |
| `principal_type` | string | Filter by type: HUMAN, SERVICE, SYSTEM, DEVICE |
| `status` | string | Filter by status: PENDING, ACTIVE, SUSPENDED, INACTIVE, DELETED |
| `tenant_id` | UUID | Filter by tenant membership |
| `page` | int | Page number (1-indexed, default: 1) |
| `size` | int | Page size (default: 50, max: 100) |

### Service Principals

#### Create Service Principal
```http
POST /api/v1/iam/principals/service
Content-Type: application/json
Authorization: Bearer <token>

{
  "service_name": "PaymentService",
  "primary_tenant_id": "550e8400-e29b-41d4-a716-446655440000",
  "allowed_scopes": ["payments.read", "payments.write", "orders.read"]
}
```

**Response (201 Created):**
```json
{
  "principal_id": "789e0123-e89b-12d3-a456-426614174000",
  "username": "paymentservice",
  "status": "ACTIVE",
  "api_key": "sk_live_abc123def456...",
  "keycloak_client_id": "payment-service-client",
  "keycloak_client_secret": "client-secret-xyz",
  "credential_rotation_date": "2024-04-15"
}
```

#### Rotate Credentials
```http
POST /api/v1/iam/principals/{principalId}/rotate-credentials
Content-Type: application/json
Authorization: Bearer <token>

{
  "force": true,
  "reason": "Security incident response"
}
```

**Response (200 OK):**
```json
{
  "principal_id": "789e0123-e89b-12d3-a456-426614174000",
  "api_key": "sk_live_new123new456...",
  "keycloak_client_secret": "new-client-secret",
  "credential_rotation_date": "2024-07-15",
  "rotated_at": "2024-04-15T14:30:00Z"
}
```

### System Principals

#### Create System Principal
```http
POST /api/v1/iam/principals/system
Content-Type: application/json
Authorization: Bearer <token>

{
  "system_identifier": "ERP_SAP_001",
  "integration_type": "ERP",
  "primary_tenant_id": "550e8400-e29b-41d4-a716-446655440000",
  "certificate_thumbprint": "SHA256:abc123def456789...",
  "allowed_operations": ["inventory.read", "orders.write", "products.sync"]
}
```

**Response (201 Created):**
```json
{
  "principal_id": "456e7890-e89b-12d3-a456-426614174000",
  "username": "erp_sap_001",
  "system_identifier": "ERP_SAP_001",
  "integration_type": "ERP",
  "status": "ACTIVE"
}
```

### Device Principals

#### Create Device Principal
```http
POST /api/v1/iam/principals/device
Content-Type: application/json
Authorization: Bearer <token>

{
  "device_identifier": "SENSOR_TEMP_001",
  "device_type": "IOT_SENSOR",
  "primary_tenant_id": "550e8400-e29b-41d4-a716-446655440000",
  "manufacturer": "Acme Sensors Inc.",
  "model": "TempSensor X200",
  "firmware_version": "v2.1.0",
  "certificate_thumbprint": "SHA256:device123abc...",
  "location_info": {
    "building": "Warehouse A",
    "floor": "3",
    "zone": "Cold Storage"
  }
}
```

**Response (201 Created):**
```json
{
  "principal_id": "def01234-e89b-12d3-a456-426614174000",
  "username": "sensor_temp_001",
  "device_identifier": "SENSOR_TEMP_001",
  "device_type": "IOT_SENSOR",
  "status": "ACTIVE"
}
```

#### Update Device Heartbeat
```http
POST /api/v1/iam/principals/{principalId}/heartbeat
Content-Type: application/json
Authorization: Bearer <token>

{
  "firmware_version": "v2.2.0",
  "location_info": {
    "building": "Warehouse B",
    "floor": "1"
  }
}
```

**Response (200 OK):**
```json
{
  "principal_id": "def01234-e89b-12d3-a456-426614174000",
  "last_heartbeat_at": "2024-01-15T15:45:30Z"
}
```

### Lifecycle Operations

#### Activate Principal
```http
POST /api/v1/iam/principals/{principalId}/activate
Content-Type: application/json
Authorization: Bearer <token>

{
  "reason": "Account verification completed"
}
```

#### Suspend Principal
```http
POST /api/v1/iam/principals/{principalId}/suspend
Content-Type: application/json
Authorization: Bearer <token>

{
  "reason": "Security review required",
  "duration_days": 7
}
```

#### Deactivate Principal
```http
POST /api/v1/iam/principals/{principalId}/deactivate
Content-Type: application/json
Authorization: Bearer <token>

{
  "reason": "Employee offboarding"
}
```

#### GDPR Delete
```http
DELETE /api/v1/iam/principals/{principalId}/gdpr
Content-Type: application/json
Authorization: Bearer <token>

{
  "confirmation": "DELETE",
  "gdpr_request_ticket": "GDPR-2024-001234",
  "requestor_email": "dpo@company.com"
}
```

### Tenant Membership

#### List Tenant Memberships
```http
GET /api/v1/iam/principals/{principalId}/tenants?page=1&size=50
Authorization: Bearer <token>
```

#### Add Tenant Membership
```http
POST /api/v1/iam/principals/{principalId}/tenants
Content-Type: application/json
Authorization: Bearer <token>

{
  "tenant_id": "660e8400-e29b-41d4-a716-446655440000",
  "valid_from": "2024-01-15",
  "valid_to": "2024-12-31"
}
```

#### Remove Tenant Membership
```http
DELETE /api/v1/iam/principals/{principalId}/tenants/{tenantId}
Authorization: Bearer <token>
```

### Credential Status
```http
GET /api/v1/iam/principals/{principalId}/credentials/status
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "principal_id": "789e0123-e89b-12d3-a456-426614174000",
  "has_api_key": true,
  "has_certificate": false,
  "credential_rotation_date": "2024-04-15",
  "days_until_rotation": 30,
  "rotation_required": false
}
```

### Admin Operations

#### Cross-Tenant Search
```http
GET /api/v1/iam/admin/principals?search=john&principal_type=HUMAN&status=ACTIVE&page=1&size=50
Authorization: Bearer <token>
```

---

## Principal Types

| Type | Description | Authentication Method |
|------|-------------|----------------------|
| **HUMAN** | End users (employees, customers) | OAuth2/OIDC via Keycloak |
| **SERVICE** | Internal microservices | API Key + OAuth2 Client Credentials |
| **SYSTEM** | External integrations (ERP, CRM) | Certificate-based (mTLS) |
| **DEVICE** | IoT sensors, edge devices | Certificate-based |

### Device Types

| Type | Description |
|------|-------------|
| `IOT_SENSOR` | Temperature, humidity, motion sensors |
| `EDGE_DEVICE` | Edge computing nodes |
| `KIOSK` | Interactive kiosks and terminals |
| `TERMINAL` | POS terminals, workstations |
| `GATEWAY` | IoT gateways, protocol translators |
| `OTHER` | Custom device types |

### Integration Types (System Principals)

| Type | Description |
|------|-------------|
| `ERP` | Enterprise Resource Planning (SAP, Oracle) |
| `CRM` | Customer Relationship Management |
| `PAYMENT` | Payment processors |
| `SHIPPING` | Logistics and shipping providers |
| `OTHER` | Custom integrations |

---

## Principal Lifecycle

```
                    ┌─────────┐
                    │ PENDING │ ← Initial state (awaiting verification)
                    └────┬────┘
                         │ activate
                         ▼
    ┌───────────────►┌────────┐◄───────────────┐
    │                │ ACTIVE │                │
    │                └───┬────┘                │
    │                    │                     │
    │ activate           │ suspend             │ activate
    │                    ▼                     │
    │               ┌──────────┐               │
    └───────────────┤SUSPENDED ├───────────────┘
                    └────┬─────┘
                         │ deactivate
                         ▼
                    ┌──────────┐
                    │ INACTIVE │
                    └────┬─────┘
                         │ gdpr_delete
                         ▼
                    ┌─────────┐
                    │ DELETED │ ← Terminal state (soft delete)
                    └─────────┘
```

---

## Database Schema

The service uses PostgreSQL with the `iam_principal` schema containing:

| Table | Description |
|-------|-------------|
| `human_principals` | Human user accounts with profile data |
| `service_principals` | Internal service accounts |
| `system_principals` | External system integrations |
| `device_principals` | IoT and device identities |
| `principal_tenant_memberships` | Many-to-many tenant associations |

Database migrations are managed by Flyway and located in `src/main/resources/db/migration/`.

---

## Testing

```bash
# Run all tests
./mvnw test

# Run with coverage report
./mvnw test jacoco:report

# Run specific test class
./mvnw test -Dtest=HumanPrincipalServiceTest

# Run integration tests only
./mvnw test -Dtest=*IT

# Run unit tests only
./mvnw test -Dtest=*Test
```

### Test Categories

- **Unit Tests** (`*Test.java`): Service layer logic with mocked dependencies
- **Integration Tests** (`*IT.java`): Controller layer with MockMvc and `@WebMvcTest`
- **Repository Tests**: JPA repository tests with `@DataJpaTest`

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `POSTGRES_HOST` | `localhost` | PostgreSQL host |
| `POSTGRES_PORT` | `5432` | PostgreSQL port |
| `POSTGRES_DATABASE` | `postgres` | Database name |
| `POSTGRES_USERNAME` | `postgres` | Database username |
| `POSTGRES_PASSWORD` | `postgres` | Database password |
| `KEYCLOAK_SERVER_URL` | `http://localhost:8080` | Keycloak server URL |
| `KEYCLOAK_REALM` | `openleap-realm` | Keycloak realm name |
| `KEYCLOAK_CLIENT_ID` | `datapart-authorization` | OAuth2 client ID |
| `KEYCLOAK_CLIENT_SECRET` | - | OAuth2 client secret |
| `KEYCLOAK_ADMIN_USERNAME` | `admin` | Keycloak admin username |
| `KEYCLOAK_ADMIN_PASSWORD` | `admin` | Keycloak admin password |
| `CONFIG_SERVER_HOST` | `localhost` | Spring Cloud Config server host |
| `CONFIG_SERVER_PORT` | `8099` | Spring Cloud Config server port |
| `CONFIG_SERVER_USERNAME` | `user` | Config server username |
| `CONFIG_SERVER_PASSWORD` | `sa` | Config server password |

---

## Spring Profiles

| Profile | Description |
|---------|-------------|
| `logger` | Local development with console logging, no Eureka/Config Server |
| `keycloak` | Enables Keycloak integration with Config Server import |
| `keycloak.web` | Keycloak with web security configuration |

### Example Profile Combinations

```bash
# Local development
java -jar app.jar --spring.profiles.active=logger

# Production with Keycloak
java -jar app.jar --spring.profiles.active=keycloak

# Full integration
java -jar app.jar --spring.profiles.active=keycloak,keycloak.web
```

---

## License

Copyright (c) OpenLeap. All rights reserved.
