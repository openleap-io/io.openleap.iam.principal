package io.openleap.iam.principal.service;

import io.openleap.common.messaging.event.EventPublisher;
import io.openleap.iam.principal.domain.dto.*;
import io.openleap.iam.principal.domain.entity.HumanPrincipalEntity;
import io.openleap.iam.principal.domain.entity.PrincipalId;
import io.openleap.iam.principal.domain.entity.PrincipalStatus;
import io.openleap.iam.principal.domain.entity.SyncStatus;
import io.openleap.iam.principal.domain.event.PrincipalCreatedEvent;
import io.openleap.iam.principal.domain.event.ProfileUpdatedEvent;
import io.openleap.iam.principal.domain.mapper.HumanPrincipalMapper;
import io.openleap.iam.principal.exception.EmailAlreadyExistsException;
import io.openleap.iam.principal.exception.InactivePrincipalFoundException;
import io.openleap.iam.principal.exception.UsernameAlreadyExistsException;
import io.openleap.iam.principal.repository.HumanPrincipalRepository;
import io.openleap.iam.principal.service.keycloak.KeycloakService;
import io.openleap.iam.principal.service.keycloak.dto.User;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class HumanPrincipalService {

    private static final Logger logger = LoggerFactory.getLogger(HumanPrincipalService.class);

    private final HumanPrincipalRepository humanPrincipalRepository;
    private final TenantService tenantService;
    private final KeycloakService keycloakService;
    private final EventPublisher eventPublisher;
    private final HumanPrincipalMapper humanPrincipalMapper;


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
    private static final String PROFILE_UPDATED_KEY = "iam.principal.profile.updated";

    private static final String NO_DESC = "nodesc";

    public HumanPrincipalService(
            HumanPrincipalRepository humanPrincipalRepository,
            KeycloakService keycloakService,
            TenantService tenantService,
            EventPublisher eventPublisher,
            HumanPrincipalMapper humanPrincipalMapper) {
        this.humanPrincipalRepository = humanPrincipalRepository;
        this.keycloakService = keycloakService;
        this.tenantService = tenantService;
        this.eventPublisher = eventPublisher;
        this.humanPrincipalMapper = humanPrincipalMapper;
    }

    @Transactional
    public HumanPrincipalCreated createHumanPrincipal(CreateHumanPrincipalCommand command) {
        if (humanPrincipalRepository.existsByUsername(command.username())) {
            throw new UsernameAlreadyExistsException(command.username());
        }

        var inactivePrincipal = humanPrincipalRepository.findInactiveByEmail(command.email());
        if (inactivePrincipal.isPresent()) {
            throw new InactivePrincipalFoundException(
                    inactivePrincipal.get().getBusinessId().value(),
                    command.email()
            );
        }

        if (humanPrincipalRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException(command.email());
        }

        HumanPrincipalEntity principal = createHumanPrincipalEntity(command);

        principal = humanPrincipalRepository.save(principal);

        //TODO: call iam.tenant for membership

        String keycloakUserId = createUserInKeycloak(principal);

        principal.setSyncStatus(SyncStatus.SYNCED);
        principal.setKeycloakUserId(keycloakUserId);
        humanPrincipalRepository.save(principal);

        PrincipalCreatedEvent event = humanPrincipalMapper.toPrincipalCreatedEvent(principal);

        //TODO: add messaging
//        RoutingKey routingKey = new RoutingKey(PRINCIPAL_CREATED_KEY, NO_DESC, "", "");
//        eventPublisher.enqueue(IAM_PRINCIPAL_EXCHANGE, routingKey, null, Collections.emptyMap());

        return humanPrincipalMapper.toHumanPrincipalCreated(principal);
    }

    private String createUserInKeycloak(HumanPrincipalEntity principal) {
        String keycloakUserId;
        try {
            // Create user in Keycloak
            User keycloakUser = User.builder()
                    .id(principal.getBusinessId().toString())
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
        return keycloakUserId;
    }

    private static @NonNull HumanPrincipalEntity createHumanPrincipalEntity(CreateHumanPrincipalCommand command) {
        HumanPrincipalEntity principal = new HumanPrincipalEntity();
        principal.setBusinessId(PrincipalId.create());
        principal.setUsername(command.username());
        principal.setFirstName(command.firstName());
        principal.setLastName(command.lastName());
        principal.setEmail(command.email());
        principal.setDefaultTenantId(command.defaultTenantId());
        principal.setStatus(PrincipalStatus.PENDING);
        principal.setSyncStatus(SyncStatus.PENDING);
        principal.setContextTags(command.contextTags());

        if (command.displayName() != null && !command.displayName().isBlank()) {
            principal.setDisplayName(command.displayName());
        } else {
            principal.setDisplayName(command.username());
        }
        principal.setPhone(command.phone());
        principal.setLanguage(command.language());
        principal.setTimezone(command.timezone());
        principal.setLocale(command.locale());
        principal.setAvatarUrl(command.avatarUrl());
        principal.setBio(command.bio());
        principal.setPreferences(command.preferences());
        return principal;
    }

    @Transactional
    public ProfileUpdated updateProfile(UpdateProfileCommand command) {
        // Load principal
        HumanPrincipalEntity principal = humanPrincipalRepository.findByBusinessId(PrincipalId.of(command.id()))
                .orElseThrow(() -> new RuntimeException("Principal not found: " + command.id()));

        // Validate principal is ACTIVE
        if (principal.getStatus() != PrincipalStatus.ACTIVE) {
            throw new InactivePrincipalFoundException(principal.getBusinessId().value(), principal.getEmail());
        }

        // Track changed fields
        java.util.List<String> changedFields = new java.util.ArrayList<>();

        // Update profile fields if provided
        if (command.firstName() != null && !command.firstName().equals(principal.getFirstName())) {
            principal.setFirstName(command.firstName());
            changedFields.add("first_name");
        }
        if (command.lastName() != null && !command.lastName().equals(principal.getLastName())) {
            principal.setLastName(command.lastName());
            changedFields.add("last_name");
        }
        if (command.displayName() != null && !command.displayName().isBlank()
                && !command.displayName().equals(principal.getDisplayName())) {
            principal.setDisplayName(command.displayName());
            changedFields.add("display_name");
        }
        if (command.phone() != null && !command.phone().equals(principal.getPhone())) {
            principal.setPhone(command.phone());
            changedFields.add("phone");
        }
        if (command.language() != null && !command.language().equals(principal.getLanguage())) {
            principal.setLanguage(command.language());
            changedFields.add("language");
        }
        if (command.timezone() != null && !command.timezone().equals(principal.getTimezone())) {
            principal.setTimezone(command.timezone());
            changedFields.add("timezone");
        }
        if (command.locale() != null && !command.locale().equals(principal.getLocale())) {
            principal.setLocale(command.locale());
            changedFields.add("locale");
        }
        if (command.avatarUrl() != null && !command.avatarUrl().equals(principal.getAvatarUrl())) {
            principal.setAvatarUrl(command.avatarUrl());
            changedFields.add("avatar_url");
        }
        if (command.bio() != null && !command.bio().equals(principal.getBio())) {
            principal.setBio(command.bio());
            changedFields.add("bio");
        }
        if (command.preferences() != null && !command.preferences().equals(principal.getPreferences())) {
            principal.setPreferences(command.preferences());
            changedFields.add("preferences");
        }
        if (command.contextTags() != null && !command.contextTags().equals(principal.getContextTags())) {
            principal.setContextTags(command.contextTags());
            changedFields.add("context_tags");
        }

        // If no fields changed, return early
        if (changedFields.isEmpty()) {
            return new ProfileUpdated(principal.getBusinessId().value(), Collections.emptyList());
        }

        // Save principal
        principal = humanPrincipalRepository.save(principal);

        // Sync relevant fields to Keycloak (display_name, first_name, last_name)
        if (principal.getKeycloakUserId() != null && !principal.getKeycloakUserId().isBlank()) {
            try {
                User keycloakUser = User.builder()
                        .id(principal.getBusinessId().toString())
                        .username(principal.getUsername())
                        .email(principal.getEmail())
                        .firstName(principal.getFirstName())
                        .lastName(principal.getLastName())
                        .enabled(principal.getStatus() == PrincipalStatus.ACTIVE)
                        .emailVerified(principal.getEmailVerified())
                        .build();
                keycloakService.updateUser(principal.getKeycloakUserId(), keycloakUser);
            } catch (Exception e) {
                // Log error but don't fail the transaction - profile update should succeed even if Keycloak sync fails
                logger.error("Failed to sync profile update to Keycloak for principal: " + principal.getBusinessId(), e);
            }
        }

        // Publish event
        ProfileUpdatedEvent event = new ProfileUpdatedEvent(
                principal.getBusinessId().value(),
                principal.getBusinessId().value(), // TODO: Get actual updated_by from security context
                changedFields
        );

//        RoutingKey routingKey = new RoutingKey(PROFILE_UPDATED_KEY, NO_DESC, "", "");
//        eventPublisher.enqueue(IAM_PRINCIPAL_EXCHANGE, routingKey, null, Collections.emptyMap());
        return new ProfileUpdated(principal.getBusinessId().value(), changedFields);
    }


    @Transactional(readOnly = true)
    public ProfileDetails getProfile(java.util.UUID principalId) {
        HumanPrincipalEntity principal = humanPrincipalRepository.findByBusinessId(principalId)
                .orElseThrow(() -> new RuntimeException("Principal not found: " + principalId));
        return humanPrincipalMapper.toProfileDetails(principal);
    }
}
