package io.openleap.iam.principal.service;

import io.openleap.iam.principal.domain.dto.CreateHumanPrincipalCommand;
import io.openleap.iam.principal.domain.dto.HumanPrincipalCreated;
import io.openleap.iam.principal.domain.entity.*;
import io.openleap.iam.principal.domain.event.PrincipalCreatedEvent;
import io.openleap.iam.principal.exception.EmailAlreadyExistsException;
import io.openleap.iam.principal.exception.InactivePrincipalFoundException;
import io.openleap.iam.principal.exception.TenantNotFoundException;
import io.openleap.iam.principal.exception.UsernameAlreadyExistsException;
import io.openleap.iam.principal.repository.HumanPrincipalRepository;
import io.openleap.iam.principal.repository.PrincipalTenantMembershipRepository;
import io.openleap.iam.principal.service.keycloak.KeycloakService;
import io.openleap.iam.principal.service.keycloak.dto.User;
import io.openleap.starter.core.messaging.RoutingKey;
import io.openleap.starter.core.messaging.event.EventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;

@Service
public class HumanPrincipalService {

    private final HumanPrincipalRepository humanPrincipalRepository;
    private final PrincipalTenantMembershipRepository membershipRepository;
    private final TenantService tenantService;
    private final KeycloakService keycloakService;
    private final EventPublisher eventPublisher;


    private static final String IAM_PRINCIPAL_EXCHANGE = "iam.principal.events";

    private static final String PRINCIPAL_CREATED_KEY = "iam.principal.principal.created";
    @SuppressWarnings("unused")
    private static final String PRINCIPAL_ACTIVATED_KEY = "iam.principal.principal.activated";
    @SuppressWarnings("unused")
    private static final String PRINCIPAL_SUSPENDED_KEY = "iam.principal.principal.suspended";
    @SuppressWarnings("unused")
    private static final String PRINCIPAL_DEACTIVATED_KEY = "iam.principal.principal.deactivated";
    @SuppressWarnings("unused")
    private static final String PRINCIPAL_DELETED_KEY = "iam.principal.principal.deleted";

    private static final String NO_DESC = "nodesc";

    public HumanPrincipalService(
            HumanPrincipalRepository humanPrincipalRepository,
            PrincipalTenantMembershipRepository membershipRepository,
            TenantService tenantService,
            KeycloakService keycloakService,
            EventPublisher eventPublisher) {
        this.humanPrincipalRepository = humanPrincipalRepository;
        this.membershipRepository = membershipRepository;
        this.tenantService = tenantService;
        this.keycloakService = keycloakService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public HumanPrincipalCreated createHumanPrincipal(CreateHumanPrincipalCommand command) {
        // Validate username uniqueness
        if (humanPrincipalRepository.existsByUsername(command.username())) {
            throw new UsernameAlreadyExistsException(command.username());
        }

        // Validate email uniqueness
        // Check for inactive principal with same email (Alt-1)
        var inactivePrincipal = humanPrincipalRepository.findInactiveByEmail(command.email());
        if (inactivePrincipal.isPresent()) {
            throw new InactivePrincipalFoundException(
                    inactivePrincipal.get().getPrincipalId(),
                    command.email()
            );
        }

        // Check if email already exists (active principal)
        if (humanPrincipalRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException(command.email());
        }

        // Validate primary tenant exists
        if (!tenantService.tenantExists(command.primaryTenantId())) {
            throw new TenantNotFoundException(command.primaryTenantId());
        }

        // Create HumanPrincipalEntity
        HumanPrincipalEntity principal = new HumanPrincipalEntity();
        principal.setUsername(command.username());
        principal.setFirstName(command.firstName());
        principal.setLastName(command.lastName());
        principal.setEmail(command.email());
        principal.setPrimaryTenantId(command.primaryTenantId());
        principal.setStatus(PrincipalStatus.PENDING);
        principal.setSyncStatus(SyncStatus.PENDING);
        principal.setContextTags(command.contextTags());

        // Set profile fields with defaults if not provided
        if (command.displayName() != null && !command.displayName().isBlank()) {
            principal.setDisplayName(command.displayName());
        } else {
            // Default display name from username
            principal.setDisplayName(command.username());
        }

        principal.setPhone(command.phone());
        principal.setLanguage(command.language());
        principal.setTimezone(command.timezone());
        principal.setLocale(command.locale());
        principal.setAvatarUrl(command.avatarUrl());
        principal.setBio(command.bio());
        principal.setPreferences(command.preferences());

        // Save principal
        principal = humanPrincipalRepository.save(principal);

        // Create PrincipalTenantMembership for primary tenant
        PrincipalTenantMembershipEntity membership = new PrincipalTenantMembershipEntity();
        membership.setPrincipalId(principal.getPrincipalId());
        membership.setPrincipalType(PrincipalType.HUMAN);
        membership.setTenantId(command.primaryTenantId());
        membership.setValidFrom(LocalDate.now());
        membership.setStatus(io.openleap.iam.principal.domain.entity.MembershipStatus.ACTIVE);

        membershipRepository.save(membership);

        String keycloakUserId;
        try {
            // Create user in Keycloak
            User keycloakUser = User.builder()
                    .id(principal.getPrincipalId().toString())
                    .username(principal.getUsername())
                    .email(principal.getEmail())
                    .firstName(principal.getFirstName())
                    .lastName(principal.getLastName())
                    .enabled(false)
                    .emailVerified(false)
                    .build();
            keycloakUserId = keycloakService.createUser(keycloakUser);
        } catch (Exception e) {
            // Rollback transaction by throwing a runtime exception
            throw new RuntimeException("Failed to create user in Keycloak: " + e.getMessage(), e);
        }

        principal.setSyncStatus(SyncStatus.SYNCED);
        principal.setKeycloakUserId(keycloakUserId);
        humanPrincipalRepository.save(principal);


        PrincipalCreatedEvent event = new PrincipalCreatedEvent(
                principal.getPrincipalId(),
                principal.getPrincipalType().name(),
                principal.getUsername(),
                principal.getEmail(),
                principal.getPrimaryTenantId(),
                principal.getStatus().name(),
                principal.getCreatedBy()
        );


        //TODO: payload fix
        RoutingKey routingKey = new RoutingKey(PRINCIPAL_CREATED_KEY, NO_DESC, "", "");
        eventPublisher.enqueue(IAM_PRINCIPAL_EXCHANGE, routingKey, null, Collections.emptyMap());

        return new HumanPrincipalCreated(principal.getPrincipalId());
    }
}
