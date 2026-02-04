package io.openleap.iam.principal.service;

import io.openleap.common.messaging.event.EventPublisher;
import io.openleap.iam.principal.domain.dto.CreateServicePrincipalCommand;
import io.openleap.iam.principal.domain.dto.CredentialsRotated;
import io.openleap.iam.principal.domain.dto.RotateCredentialsCommand;
import io.openleap.iam.principal.domain.dto.ServicePrincipalCreated;
import io.openleap.iam.principal.domain.entity.PrincipalId;
import io.openleap.iam.principal.domain.entity.PrincipalStatus;
import io.openleap.iam.principal.domain.entity.ServicePrincipalEntity;
import io.openleap.iam.principal.domain.entity.SyncStatus;
import io.openleap.iam.principal.domain.event.CredentialsRotatedEvent;
import io.openleap.iam.principal.domain.event.ServicePrincipalCreatedEvent;
import io.openleap.iam.principal.domain.mapper.ServicePrincipalMapper;
import io.openleap.iam.principal.exception.ServiceNameAlreadyExistsException;
import io.openleap.iam.principal.exception.UsernameAlreadyExistsException;
import io.openleap.iam.principal.repository.ServicePrincipalRepository;
import io.openleap.iam.principal.service.keycloak.KeycloakService;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

@Service
public class ServicePrincipalService {

    private final ServicePrincipalRepository servicePrincipalRepository;
    private final KeycloakService keycloakService;
    private final CredentialService credentialService;
    private final EventPublisher eventPublisher;
    private final ServicePrincipalMapper servicePrincipalMapper;

    private static final String IAM_PRINCIPAL_EXCHANGE = "iam.principal.events";
    private static final String CREDENTIALS_ROTATED_KEY = "iam.principal.credentials.rotated";
    private static final String NO_DESC = "nodesc";
    private static final int CREDENTIAL_ROTATION_DAYS = 90;

    public ServicePrincipalService(
            ServicePrincipalRepository servicePrincipalRepository,
            KeycloakService keycloakService,
            CredentialService credentialService,
            EventPublisher eventPublisher,
            ServicePrincipalMapper servicePrincipalMapper) {
        this.servicePrincipalRepository = servicePrincipalRepository;
        this.keycloakService = keycloakService;
        this.credentialService = credentialService;
        this.eventPublisher = eventPublisher;
        this.servicePrincipalMapper = servicePrincipalMapper;
    }

    @Transactional
    public ServicePrincipalCreated createServicePrincipal(CreateServicePrincipalCommand command) {

        if (servicePrincipalRepository.existsByServiceName(command.serviceName())) {
            throw new ServiceNameAlreadyExistsException(command.serviceName());
        }

        String username = command.serviceName().toLowerCase();
        if (servicePrincipalRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(username);
        }

        String apiKey = credentialService.generateApiKey();
        String apiKeyHash = credentialService.hashApiKey(apiKey);

        ServicePrincipalEntity principal = createServicePrincipalEntity(command, username, apiKeyHash);

        principal = servicePrincipalRepository.save(principal);

        String keycloakClientId = null;
        String keycloakClientSecret = null;
        try {
            keycloakClientId = principal.getServiceName();
            keycloakClientSecret = keycloakService.createClient(keycloakClientId, command.allowedScopes());

            principal.setSyncStatus(SyncStatus.SYNCED);
            principal.setKeycloakClientId(keycloakClientId);
            principal = servicePrincipalRepository.save(principal);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create client in Keycloak: " + e.getMessage(), e);
        }

        // Publish service principal created event
        ServicePrincipalCreatedEvent event = servicePrincipalMapper.toServicePrincipalCreatedEvent(principal);
//TODO: re-enable events later
//        RoutingKey routingKey = new RoutingKey(SERVICE_PRINCIPAL_CREATED_KEY, NO_DESC, "", "");
//        eventPublisher.enqueue(IAM_PRINCIPAL_EXCHANGE, routingKey, null, Collections.emptyMap());

        return servicePrincipalMapper.toServicePrincipalCreated(principal, apiKey, keycloakClientId, keycloakClientSecret);
    }

    private static @NonNull ServicePrincipalEntity createServicePrincipalEntity(CreateServicePrincipalCommand command, String username, String apiKeyHash) {
        ServicePrincipalEntity principal = new ServicePrincipalEntity();
        principal.setBusinessId(PrincipalId.create());
        principal.setUsername(username);
        principal.setServiceName(command.serviceName());
        principal.setDefaultTenantId(command.defaultTenantId());
        principal.setStatus(PrincipalStatus.ACTIVE); // Service principals are ACTIVE immediately
        principal.setSyncStatus(SyncStatus.PENDING);
        principal.setContextTags(command.contextTags());
        principal.setAllowedScopes(command.allowedScopes());
        principal.setApiKeyHash(apiKeyHash);
        principal.setCredentialRotationDate(LocalDate.now().plusDays(90)); // 90 days from now
        return principal;
    }

    @Transactional
    public CredentialsRotated rotateCredentials(RotateCredentialsCommand command) {

        ServicePrincipalEntity principal = servicePrincipalRepository.findByBusinessId(PrincipalId.of(command.id()))
                .orElseThrow(() -> new RuntimeException("Service principal not found: " + command.id()));

        if (principal.getStatus() != PrincipalStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Service principal must be ACTIVE to rotate credentials. Current status: " + principal.getStatus());
        }

        boolean rotationDue = principal.getCredentialRotationDate() != null
                && !LocalDate.now().isBefore(principal.getCredentialRotationDate());
        boolean forceRotation = command.force() != null && command.force();

        if (!rotationDue && !forceRotation) {
            throw new IllegalStateException(
                    "Credential rotation is not due. Next rotation date: " + principal.getCredentialRotationDate()
                            + ". Use force=true to rotate anyway.");
        }

        String newApiKey = credentialService.generateApiKey();
        String newApiKeyHash = credentialService.hashApiKey(newApiKey);

        Instant rotatedAt = Instant.now();
        LocalDate newRotationDate = LocalDate.now().plusDays(CREDENTIAL_ROTATION_DAYS);

        principal.setApiKeyHash(newApiKeyHash);
        principal.setCredentialRotationDate(newRotationDate);
        principal.setRotatedAt(rotatedAt);

        servicePrincipalRepository.save(principal);

        String newKeycloakClientSecret = null;
        if (principal.getKeycloakClientId() != null && !principal.getKeycloakClientId().isBlank()) {
            try {
                newKeycloakClientSecret = keycloakService.regenerateClientSecret(principal.getKeycloakClientId());
            } catch (Exception e) {
                throw new RuntimeException("Failed to regenerate client secret in Keycloak: " + e.getMessage(), e);
            }
        }

        CredentialsRotatedEvent event = servicePrincipalMapper.toCredentialsRotatedEvent(
                principal, command, newRotationDate, rotatedAt);

//        RoutingKey routingKey = new RoutingKey(CREDENTIALS_ROTATED_KEY, NO_DESC, "", "");
//        eventPublisher.enqueue(IAM_PRINCIPAL_EXCHANGE, routingKey, null, Collections.emptyMap());

        return servicePrincipalMapper.toCredentialsRotated(
                principal, newApiKey, newKeycloakClientSecret, newRotationDate, rotatedAt);
    }
}
