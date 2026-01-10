package io.openleap.iam.principal.service;

import io.openleap.iam.principal.domain.dto.CreateServicePrincipalCommand;
import io.openleap.iam.principal.domain.dto.ServicePrincipalCreated;
import io.openleap.iam.principal.domain.entity.*;
import io.openleap.iam.principal.domain.event.ServicePrincipalCreatedEvent;
import io.openleap.iam.principal.exception.ServiceNameAlreadyExistsException;
import io.openleap.iam.principal.exception.TenantNotFoundException;
import io.openleap.iam.principal.exception.UsernameAlreadyExistsException;
import io.openleap.iam.principal.repository.PrincipalTenantMembershipRepository;
import io.openleap.iam.principal.repository.ServicePrincipalRepository;
import io.openleap.iam.principal.service.keycloak.KeycloakService;
import io.openleap.starter.core.messaging.RoutingKey;
import io.openleap.starter.core.messaging.event.EventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;

@Service
public class ServicePrincipalService {

    private final ServicePrincipalRepository servicePrincipalRepository;
    private final PrincipalTenantMembershipRepository membershipRepository;
    private final TenantService tenantService;
    private final KeycloakService keycloakService;
    private final CredentialService credentialService;
    private final EventPublisher eventPublisher;

    private static final String IAM_PRINCIPAL_EXCHANGE = "iam.principal.events";
    private static final String SERVICE_PRINCIPAL_CREATED_KEY = "iam.principal.service_principal.created";
    private static final String NO_DESC = "nodesc";

    public ServicePrincipalService(
            ServicePrincipalRepository servicePrincipalRepository,
            PrincipalTenantMembershipRepository membershipRepository,
            TenantService tenantService,
            KeycloakService keycloakService,
            CredentialService credentialService,
            EventPublisher eventPublisher) {
        this.servicePrincipalRepository = servicePrincipalRepository;
        this.membershipRepository = membershipRepository;
        this.tenantService = tenantService;
        this.keycloakService = keycloakService;
        this.credentialService = credentialService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ServicePrincipalCreated createServicePrincipal(CreateServicePrincipalCommand command) {
        // Validate service name uniqueness
        if (servicePrincipalRepository.existsByServiceName(command.serviceName())) {
            throw new ServiceNameAlreadyExistsException(command.serviceName());
        }

        // Validate username uniqueness (service name becomes username)
        String username = command.serviceName().toLowerCase();
        if (servicePrincipalRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(username);
        }

        // Validate primary tenant exists
        if (!tenantService.tenantExists(command.primaryTenantId())) {
            throw new TenantNotFoundException(command.primaryTenantId());
        }

        // Generate API key
        String apiKey = credentialService.generateApiKey();
        String apiKeyHash = credentialService.hashApiKey(apiKey);

        // Create ServicePrincipalEntity
        ServicePrincipalEntity principal = new ServicePrincipalEntity();
        principal.setUsername(username);
        principal.setServiceName(command.serviceName());
        principal.setPrimaryTenantId(command.primaryTenantId());
        principal.setStatus(PrincipalStatus.ACTIVE); // Service principals are ACTIVE immediately
        principal.setSyncStatus(SyncStatus.PENDING);
        principal.setContextTags(command.contextTags());
        principal.setAllowedScopes(command.allowedScopes());
        principal.setApiKeyHash(apiKeyHash);
        principal.setCredentialRotationDate(LocalDate.now().plusDays(90)); // 90 days from now

        // Save principal
        principal = servicePrincipalRepository.save(principal);

        // Create PrincipalTenantMembership for primary tenant
        PrincipalTenantMembershipEntity membership = new PrincipalTenantMembershipEntity();
        membership.setPrincipalId(principal.getPrincipalId());
        membership.setPrincipalType(PrincipalType.SERVICE);
        membership.setTenantId(command.primaryTenantId());
        membership.setValidFrom(LocalDate.now());
        membership.setStatus(MembershipStatus.ACTIVE);

        membershipRepository.save(membership);

        String keycloakClientId = null;
        String keycloakClientSecret = null;
        try {
            // Create OAuth2 client in Keycloak
            keycloakClientId = principal.getServiceName();
            keycloakClientSecret = keycloakService.createClient(keycloakClientId, command.allowedScopes());
            
            principal.setSyncStatus(SyncStatus.SYNCED);
            principal.setKeycloakClientId(keycloakClientId);
            servicePrincipalRepository.save(principal);
        } catch (Exception e) {
            // Rollback transaction by throwing a runtime exception
            throw new RuntimeException("Failed to create client in Keycloak: " + e.getMessage(), e);
        }

        // Publish service principal created event
        ServicePrincipalCreatedEvent event = new ServicePrincipalCreatedEvent(
                principal.getPrincipalId(),
                principal.getPrincipalType().name(),
                principal.getUsername(),
                principal.getServiceName(),
                principal.getPrimaryTenantId(),
                principal.getStatus().name(),
                principal.getAllowedScopes(),
                principal.getCredentialRotationDate(),
                principal.getCreatedBy()
        );

        RoutingKey routingKey = new RoutingKey(SERVICE_PRINCIPAL_CREATED_KEY, NO_DESC, "", "");
        eventPublisher.enqueue(IAM_PRINCIPAL_EXCHANGE, routingKey, null, Collections.emptyMap());

        return new ServicePrincipalCreated(
                principal.getPrincipalId(),
                principal.getUsername(),
                principal.getServiceName(),
                apiKey, // Return plain-text API key (ONLY TIME)
                keycloakClientId,
                keycloakClientSecret, // Return client secret (ONLY TIME)
                principal.getAllowedScopes(),
                principal.getCredentialRotationDate()
        );
    }
}
