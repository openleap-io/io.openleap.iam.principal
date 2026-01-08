# service-template

This is a template repository for creating new services. It includes a basic structure and configuration files to help
you get started quickly.

<!-- TOC -->
* [service-template](#service-template)
* [Getting Started](#getting-started)
  * [Local Development](#local-development)
    * [Prerequisites](#prerequisites)
    * [Building the Service](#building-the-service)
    * [Running the Service Locally](#running-the-service-locally)
      * [Option 1: With Security and Keycloak](#option-1-with-security-and-keycloak)
        * [Using the API with Authentication](#using-the-api-with-authentication)
        * [Authenticate](#authenticate)
        * [Request to the Service API](#request-to-the-service-api)
      * [Option 2: Without Security](#option-2-without-security)
        * [Using the API](#using-the-api)
  * [Deployment on T-Ubuntu22-ERP](#deployment-on-t-ubuntu22-erp)
    * [Changes to infra-setup](#changes-to-infra-setup)
    * [Using the template API in T-Ubuntu22-ERP](#using-the-template-api-in-t-ubuntu22-erp)
      * [Authenticate and get a token](#authenticate-and-get-a-token)
      * [Request to the Service API](#request-to-the-service-api-1)
<!-- TOC -->

# Getting Started

## Local Development

### Prerequisites

- Docker and Docker Compose installed on your machine.
- Java 17 or higher installed.
- Maven installed.

### Building the Service

1. Clone the repository:
```bash
git clone git@github.com:openleap-io/io.openleap.template.git
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
mvn spring-boot:run -Dspring-boot.run.profiles=keycloak
```
##### Using the API with Authentication

##### Authenticate

```bash
curl --location 'http://keycloak-web:8090/realms/openleap-realm/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--header 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJONDNuYWZpQmdhUy1OVExTMVQwNzF6aDhvR29PNGtKVnZ4blVLV3ZoaG5FIn0.eyJleHAiOjE3NTcyNjA1NzgsImlhdCI6MTc1NzI2MDI3OCwiYXV0aF90aW1lIjoxNzU3MjYwMjc2LCJqdGkiOiIwYjEyMGRkYi1jZmUwLTQ3OGEtYmQxMy0zYzE2MTEyYWM5OTUiLCJpc3MiOiJodHRwOi8va2V5Y2xvYWstd2ViOjgwOTAvcmVhbG1zL29wZW5sZWFwLXJlYWxtIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjlhZTI4YzFiLTM0NDItNGMxYy1iZDVkLWFmMmEyNTU3MjBkNCIsInR5cCI6IkJlYXJlciIsImF6cCI6ImRhdGFwYXJ0LWRvY3VtZW50LW1hbmFnZW1lbnQiLCJzaWQiOiIyY2MzNjRiOC04YTBkLTRiZmItOGQyNC1kY2Y0MDRmZGI1MDciLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbIi8qIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyIwMDE2NDEwMDAwMF9zZXR0bGVtZW50Iiwib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiIsImRlZmF1bHQtcm9sZXMtb3BlbmxlYXAtcmVhbG0iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6ImRtX3JlYWQgZW1haWwgcmVwb3J0LXNlcnZpY2UgcHJvZmlsZSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJyb2xlcyI6WyIwMDE2NDEwMDAwMF9zZXR0bGVtZW50Iiwib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiIsImRlZmF1bHQtcm9sZXMtb3BlbmxlYXAtcmVhbG0iXSwibmFtZSI6InVzZXJfc2V0dGxlbWVudCB1c2VyX3NldHRsZW1lbnQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJ1c2VyX3NldHRsZW1lbnQiLCJnaXZlbl9uYW1lIjoidXNlcl9zZXR0bGVtZW50IiwiZmFtaWx5X25hbWUiOiJ1c2VyX3NldHRsZW1lbnQiLCJlbWFpbCI6InVzZXJfc2V0dGxlbWVudEB1c2Vyc2V0dGxlbWVudC5jb20ifQ.gZa49dujZ0B42QAq_thbqmS65xNen6nbY6soNbj-7-1CaIzQwxihhzIwwVzwANes4s0qWAOWLMTZZFdwPaX8a3jx-Mqe_BEDE5XZyyy2OFQ9bVd01yZgX0mlnlRJCPoNeHNPIZymRI3en_5QunxJ15ICH9bDfaJ27AJT0_byn5dl6GJptX3oJLB0l0Z1uRKU4gW-YTkPQApFD1Pr0y6yN___AXyPW3MTDjCt5AhVp2v1s6vYN7FHieE0T3vkYs_ZUyd6lmw16SRty9qnKkyyd-PT19RW9cNElFHrB17sqImC18PJdSSEWiFOBkPm51fQi00410wGlEfX8qm4spIJ5w' \
--data-urlencode 'client_secret=2a6O8wicYg5NWXlcN34rT5KpRzQ6F7x5' \
--data-urlencode 'grant_type=client_credentials' \
--data-urlencode 'scope=template.read' \
--data-urlencode 'client_id=template'
```

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJibDJqaF9uZDBCTERmeU0yc2lvdVB6N1FPRXBVQ21JWW9HYTBHT0dXci1rIn0.eyJleHAiOjE3NTkyOTY5NTcsImlhdCI6MTc1OTI5NjY1NywianRpIjoiNjM2ODY1MTktMjUwYS00MzQ2LTk3N2UtMWZmYTA4ZmYwNjdlIiwiaXNzIjoiaHR0cDovL2tleWNsb2FrLXdlYjo4MDkwL3JlYWxtcy9vcGVubGVhcC1yZWFsbSIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiI5ZmYyYmM5ZC01ZDgwLTQzNWUtYTc1Ni1lYjNlYTUxMGQ2ZjYiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJ0ZW1wbGF0ZSIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiLyoiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iLCJkZWZhdWx0LXJvbGVzLW9wZW5sZWFwLXJlYWxtIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJlbWFpbCByZXBvcnQtc2VydmljZSBwcm9maWxlIGRtX3JlYWQgdGVtcGxhdGUucmVhZCIsImNsaWVudEhvc3QiOiIxOTIuMTY4LjY1LjEiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsInJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iLCJkZWZhdWx0LXJvbGVzLW9wZW5sZWFwLXJlYWxtIl0sInByZWZlcnJlZF91c2VybmFtZSI6InNlcnZpY2UtYWNjb3VudC10ZW1wbGF0ZSIsImNsaWVudEFkZHJlc3MiOiIxOTIuMTY4LjY1LjEiLCJjbGllbnRfaWQiOiJ0ZW1wbGF0ZSJ9.CrOtejuHwlHIOX7zfRjlgj9QP2xDl_eBdxA0eklXkksIOpcpp-b6nHu0WY8YE_vpvQmEm4Yf5sq9D15r5ofhKD9xPEbAgSwZ1FBdpVGRMrpk03O0SXBCVZtPYAoUEu_0GoquFO-eY3ZwWULPOdO2iRyjWgOlL6Z0AGAvrYQc8OK4hdmu-RAse_YJ6LYVUFREShBGbpUBXZYYXBYHck9PU05sTKRuGcjIYKJ3IJzeWQ5DvVUDGjlpmqHJiKIDZcFDXMtAb3NSQ6ZIzGCkHggysKXUnHjOTD0T-ch75ZW6p4vuKIx3kF8hfxxEAkLHHh2LcExo4ebsOnuVtJ8r_HYZBA",
  "expires_in": 300,
  "refresh_expires_in": 0,
  "token_type": "Bearer",
  "not-before-policy": 0,
  "scope": "email report-service profile dm_read template.read"
}
```
**Note**: On Keycloak startup `docker/import/keycloak/realm-export.json` is imported, which contains a client `template` with `client_id` `template` and a service account user with the role `template.read`.

##### Request to the Service API
```bash
curl --location 'localhost:8080/api/template/health' \
--header 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJibDJqaF9uZDBCTERmeU0yc2lvdVB6N1FPRXBVQ21JWW9HYTBHT0dXci1rIn0.eyJleHAiOjE3NTkyOTY5NTcsImlhdCI6MTc1OTI5NjY1NywianRpIjoiNjM2ODY1MTktMjUwYS00MzQ2LTk3N2UtMWZmYTA4ZmYwNjdlIiwiaXNzIjoiaHR0cDovL2tleWNsb2FrLXdlYjo4MDkwL3JlYWxtcy9vcGVubGVhcC1yZWFsbSIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiI5ZmYyYmM5ZC01ZDgwLTQzNWUtYTc1Ni1lYjNlYTUxMGQ2ZjYiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJ0ZW1wbGF0ZSIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiLyoiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iLCJkZWZhdWx0LXJvbGVzLW9wZW5sZWFwLXJlYWxtIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJlbWFpbCByZXBvcnQtc2VydmljZSBwcm9maWxlIGRtX3JlYWQgdGVtcGxhdGUucmVhZCIsImNsaWVudEhvc3QiOiIxOTIuMTY4LjY1LjEiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsInJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iLCJkZWZhdWx0LXJvbGVzLW9wZW5sZWFwLXJlYWxtIl0sInByZWZlcnJlZF91c2VybmFtZSI6InNlcnZpY2UtYWNjb3VudC10ZW1wbGF0ZSIsImNsaWVudEFkZHJlc3MiOiIxOTIuMTY4LjY1LjEiLCJjbGllbnRfaWQiOiJ0ZW1wbGF0ZSJ9.CrOtejuHwlHIOX7zfRjlgj9QP2xDl_eBdxA0eklXkksIOpcpp-b6nHu0WY8YE_vpvQmEm4Yf5sq9D15r5ofhKD9xPEbAgSwZ1FBdpVGRMrpk03O0SXBCVZtPYAoUEu_0GoquFO-eY3ZwWULPOdO2iRyjWgOlL6Z0AGAvrYQc8OK4hdmu-RAse_YJ6LYVUFREShBGbpUBXZYYXBYHck9PU05sTKRuGcjIYKJ3IJzeWQ5DvVUDGjlpmqHJiKIDZcFDXMtAb3NSQ6ZIzGCkHggysKXUnHjOTD0T-ch75ZW6p4vuKIx3kF8hfxxEAkLHHh2LcExo4ebsOnuVtJ8r_HYZBA'
```

```json
{
  "value": "healthy"
}
```

#### Option 2: Without Security
1. Run the Spring Boot application:

If you are using IntelliJ IDEA, you can use the `logger` configuration or run:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=logger
```
##### Using the API
```bash
curl --location 'localhost:8080/api/template/health'
```
```json
{
  "value": "healthy"
}
```

## Deployment on T-Ubuntu22-ERP

### Changes to infra-setup

1. Add the service to `docker-compose.base.SERVICES.yml`
```yaml
  template-service:
    image: ${templateService_image}
    pull_policy: always
    container_name: template-service
    environment:
      CONFIG_SERVER_HOST: config
      CONFIG_SERVER_USERNAME: ${configUser}
      CONFIG_SERVER_PASSWORD: ${configPassword}
      SPRING_PROFILES_ACTIVE: ${templateService_profile}
      eureka.client.service-url.defaultZone: http://${eurekaUser}:${eurekaPassword}@registry:8761/eureka
    networks:
      - tools
      - services
      - spring
```

2. Update the .env file with the following variables:
```env
templateService_image=docker.io/openleap/io.openleap.template-service:latest
templateService_profile=keycloak
```

3. Add the service configuration to `springtools/config/conf`

This is default configuration, you can change it as needed.

`template-service.yml`

```yaml
server:
  port: 0
logging:
  level:
    root: INFO
    io.openleap: DEBUG

oleap:
  eureka:
    url: ${oleap.srv.protocol}://${oleap.srv.user.name}:${oleap.srv.user.password}@${oleap.srv.hostname}:${oleap.srv.port}
    zone: ${oleap.eureka.url}/eureka/
  srv:
    protocol: http
    hostname: localhost
    port: 8761
    user:
      name: user
      password: sa

eureka:
  client:
    instance-info-replication-interval-seconds: 10 # default is 30
    registryFetchIntervalSeconds: 5 # SBA
    service-url:
      defaultZone: ${oleap.eureka.zone} # Must be camelCase
  instance:
    instance-id: ${spring.application.name}:${spring.application.instance_id:${random.value}}
    health-check-url-path: /actuator/health
    hostname: ${oleap.srv.hostname}
    # Request a lease with a validity of 5 seconds. If until then the
    # lease has not been renewed, it expires and the Eureka server can evict it.
    # Default: 90s
    lease-expiration-duration-in-seconds: 5

    # The cadence of lease renewal, i.e. how often a heartbeat is sent to Eureka server.
    # In this case every 2 seconds. Use this only for development / debugging. In production use the ...
    # Default: 30s
    lease-renewal-interval-in-seconds:    2
    metadata-map:
      config-protocol: http # The protocol used to access the config server
      username: ${spring.security.user.name}
      password: ${spring.security.user.password}
      protocol: ${oleap.srv.protocol}
      zone: ${oleap.eureka.zone}
    non-secure-port-enabled: true
    prefer-ip-address: true
    secure-port-enabled: false
```

`template-service-keycloak.yml`

```yaml
spring:
  security:
    oauth2:
      client:
        provider:
          openleap:
            issuer-uri: https://idptest.datapart-factoring.de/realms/master
            token-uri: https://idptest.datapart-factoring.de/realms/master/protocol/openid-connect/token
      resourceserver:
        jwt:
          jwk-set-uri: https://idptest.datapart-factoring.de/realms/master/protocol/openid-connect/certs

oleap:
  client:
    registration:
      registration-endpoint: http://keycloak:8090/realms/master/clients-registrations/default
      registration-username: register-client
      registration-password: *****
      token-endpoint: http://keycloak:8090/realms/master/protocol/openid-connect/token
      registration-scopes: client.create
      grant-types: client_credentials
      base-url: https://idptest.datapart-factoring.de

eureka:
  client:
    service-url:
      defaultZone: http://user:sa@registry:8761/eureka/
  instance:
    instance-id: ${spring.application.name}:${spring.application.instance_id:${random.value}}
    hostname: registry
```

4. Changes to the gateway and exposure of the service

in `springtools/config/conf` add to `openleap-gateway-keycloak-dp-test.yml`

```yaml
      routes:
        - id: template-service
          uri: lb://template-service
          predicates:
            - Path=/gw/template/**
          filters:
            - StripPrefix=1
```

### Using the template API in T-Ubuntu22-ERP

#### Authenticate and get a token

```bash
curl --location 'https://idptest.datapart-factoring.de/realms/master/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'client_secret=***' \
--data-urlencode 'grant_type=client_credentials' \
--data-urlencode 'scope=template.read' \
--data-urlencode 'client_id=template'
```

#### Request to the Service API

```bash
curl --location 'https://uniapitest.datapart-factoring.de/gw/template/health' \
--header 'Authorization: Bearer ****'
```

```json
{
  "value": "healthy"
}
```