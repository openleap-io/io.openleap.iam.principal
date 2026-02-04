package io.openleap.iam.principal.service;

import io.openleap.common.messaging.event.EventPublisher;
import io.openleap.iam.principal.domain.dto.*;
import io.openleap.iam.principal.domain.entity.*;
import io.openleap.iam.principal.domain.event.PrincipalActivatedEvent;
import io.openleap.iam.principal.domain.event.PrincipalDeactivatedEvent;
import io.openleap.iam.principal.domain.event.PrincipalDeletedEvent;
import io.openleap.iam.principal.domain.event.PrincipalSuspendedEvent;
import io.openleap.iam.principal.domain.mapper.PrincipalEventMapper;
import io.openleap.iam.principal.domain.mapper.ServicePrincipalMapper;
import io.openleap.iam.principal.domain.mapper.SystemPrincipalMapper;
import io.openleap.iam.principal.repository.DevicePrincipalRepository;
import io.openleap.iam.principal.repository.HumanPrincipalRepository;
import io.openleap.iam.principal.repository.ServicePrincipalRepository;
import io.openleap.iam.principal.repository.SystemPrincipalRepository;
import io.openleap.iam.principal.service.keycloak.KeycloakService;
import io.openleap.iam.principal.service.keycloak.dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PrincipalService {

    private static final Logger logger = LoggerFactory.getLogger(PrincipalService.class);

    private final HumanPrincipalRepository humanPrincipalRepository;
    private final ServicePrincipalRepository servicePrincipalRepository;
    private final SystemPrincipalRepository systemPrincipalRepository;
    private final DevicePrincipalRepository devicePrincipalRepository;
    private final KeycloakService keycloakService;
    private final EventPublisher eventPublisher;
    private final ServicePrincipalMapper servicePrincipalMapper;
    private final SystemPrincipalMapper systemPrincipalMapper;
    private final PrincipalEventMapper principalEventMapper;

    private static final String IAM_PRINCIPAL_EXCHANGE = "iam.principal.events";
    private static final String PRINCIPAL_ACTIVATED_KEY = "iam.principal.principal.activated";
    private static final String PRINCIPAL_SUSPENDED_KEY = "iam.principal.principal.suspended";
    private static final String PRINCIPAL_DEACTIVATED_KEY = "iam.principal.principal.deactivated";
    private static final String PRINCIPAL_DELETED_KEY = "iam.principal.principal.deleted";
    private static final String NO_DESC = "nodesc";
    private static final int GDPR_RETENTION_DAYS = 30;

    public PrincipalService(
            HumanPrincipalRepository humanPrincipalRepository,
            ServicePrincipalRepository servicePrincipalRepository,
            SystemPrincipalRepository systemPrincipalRepository,
            DevicePrincipalRepository devicePrincipalRepository,
            KeycloakService keycloakService,
            EventPublisher eventPublisher,
            ServicePrincipalMapper servicePrincipalMapper,
            SystemPrincipalMapper systemPrincipalMapper,
            PrincipalEventMapper principalEventMapper) {
        this.humanPrincipalRepository = humanPrincipalRepository;
        this.servicePrincipalRepository = servicePrincipalRepository;
        this.systemPrincipalRepository = systemPrincipalRepository;
        this.devicePrincipalRepository = devicePrincipalRepository;
        this.keycloakService = keycloakService;
        this.eventPublisher = eventPublisher;
        this.servicePrincipalMapper = servicePrincipalMapper;
        this.systemPrincipalMapper = systemPrincipalMapper;
        this.principalEventMapper = principalEventMapper;
    }

    /**
     * Finds a principal by ID across all principal types.
     *
     * @param principalId the principal ID
     * @return the principal entity if found, empty otherwise
     */
    public Optional<Principal> findPrincipalByBusinessId(PrincipalId principalId) {
        // Try each repository in order
        Optional<HumanPrincipalEntity> human = humanPrincipalRepository.findByBusinessId(principalId);
        if (human.isPresent()) {
            return Optional.of(human.get());
        }

        Optional<ServicePrincipalEntity> service = servicePrincipalRepository.findByBusinessId(principalId);
        if (service.isPresent()) {
            return Optional.of(service.get());
        }

        Optional<SystemPrincipalEntity> system = systemPrincipalRepository.findByBusinessId(principalId);
        if (system.isPresent()) {
            return Optional.of(system.get());
        }

        Optional<DevicePrincipalEntity> device = devicePrincipalRepository.findByBusinessId(principalId);
        if (device.isPresent()) {
            return Optional.of(device.get());
        }

        return Optional.empty();
    }

    /**
     * Activates a principal.
     *
     * @param command the activation command
     * @return the activation result
     */
    @Transactional
    public PrincipalActivated activatePrincipal(ActivatePrincipalCommand command) {
        // Find principal by ID
        Principal principal = findPrincipalByBusinessId(PrincipalId.of(command.id()))
                .orElseThrow(() -> new RuntimeException("Principal not found: " + command.id()));

        // Validate principal is PENDING
        if (principal.getStatus() != PrincipalStatus.PENDING) {
            throw new IllegalStateException(
                    "Principal must be PENDING to activate. Current status: " + principal.getStatus());
        }

        // Update status to ACTIVE
        principal.setStatus(PrincipalStatus.ACTIVE);

        // If it's a human principal and activation is via email verification, set email_verified
        if (principal instanceof HumanPrincipalEntity && command.verificationToken() != null) {
            HumanPrincipalEntity humanPrincipal = (HumanPrincipalEntity) principal;
            humanPrincipal.setEmailVerified(true);
            humanPrincipalRepository.save(humanPrincipal);
        } else {
            // Save based on principal type
            if (principal instanceof HumanPrincipalEntity) {
                humanPrincipalRepository.save((HumanPrincipalEntity) principal);
            } else if (principal instanceof ServicePrincipalEntity) {
                servicePrincipalRepository.save((ServicePrincipalEntity) principal);
            } else if (principal instanceof SystemPrincipalEntity) {
                systemPrincipalRepository.save((SystemPrincipalEntity) principal);
            } else if (principal instanceof DevicePrincipalEntity) {
                devicePrincipalRepository.save((DevicePrincipalEntity) principal);
            }
        }

        // Enable in Keycloak
        try {
            if (principal instanceof HumanPrincipalEntity) {
                // Enable Keycloak user for human principals
                HumanPrincipalEntity humanPrincipal = (HumanPrincipalEntity) principal;
                if (humanPrincipal.getKeycloakUserId() != null && !humanPrincipal.getKeycloakUserId().isBlank()) {
                    User keycloakUser = User.builder()
                            .id(humanPrincipal.getBusinessId().toString())
                            .username(humanPrincipal.getUsername())
                            .email(humanPrincipal.getEmail())
                            .firstName(humanPrincipal.getFirstName())
                            .lastName(humanPrincipal.getLastName())
                            .enabled(true) // Enable the user
                            .emailVerified(humanPrincipal.getEmailVerified())
                            .build();
                    keycloakService.updateUser(humanPrincipal.getKeycloakUserId(), keycloakUser);
                }
            } else if (principal instanceof ServicePrincipalEntity) {
                // Enable Keycloak client for service principals
                ServicePrincipalEntity servicePrincipal = (ServicePrincipalEntity) principal;
                if (servicePrincipal.getKeycloakClientId() != null && !servicePrincipal.getKeycloakClientId().isBlank()) {
                    keycloakService.updateClient(servicePrincipal.getKeycloakClientId(), true);
                }
            } else if (principal instanceof SystemPrincipalEntity) {
                // Enable Keycloak client for system principals
                SystemPrincipalEntity systemPrincipal = (SystemPrincipalEntity) principal;
                if (systemPrincipal.getKeycloakClientId() != null && !systemPrincipal.getKeycloakClientId().isBlank()) {
                    keycloakService.updateClient(systemPrincipal.getKeycloakClientId(), true);
                }
            } else if (principal instanceof DevicePrincipalEntity) {
                // Enable Keycloak client for device principals
                DevicePrincipalEntity devicePrincipal = (DevicePrincipalEntity) principal;
                if (devicePrincipal.getKeycloakClientId() != null && !devicePrincipal.getKeycloakClientId().isBlank()) {
                    keycloakService.updateClient(devicePrincipal.getKeycloakClientId(), true);
                }
            }
        } catch (Exception e) {
            // Log error but don't fail the transaction - activation should succeed even if Keycloak sync fails
            logger.error("Failed to enable principal in Keycloak for principal: " + principal.getBusinessId(), e);
        }

        // Determine activation method
        String activationMethod = command.adminOverride() != null && command.adminOverride()
                ? "admin_override"
                : (command.verificationToken() != null ? "email_verification" : "unknown");

        String activatedBy = command.adminOverride() != null && command.adminOverride()
                ? "admin"
                : "self";

        // Publish event
        PrincipalActivatedEvent event = new PrincipalActivatedEvent(
                principal.getBusinessId().value(),
                principal.getPrincipalType().name(),
                principal.getStatus().name(),
                activatedBy,
                activationMethod
        );

//        RoutingKey routingKey = new RoutingKey(PRINCIPAL_ACTIVATED_KEY, NO_DESC, "", "");
//        // TODO: Pass event payload when event publisher supports it
//        eventPublisher.enqueue(IAM_PRINCIPAL_EXCHANGE, routingKey, null, Collections.emptyMap());

        return new PrincipalActivated(principal.getBusinessId().value());
    }


    @Transactional
    public PrincipalSuspended suspendPrincipal(SuspendPrincipalCommand command) {

        Principal principal = findPrincipalByBusinessId(PrincipalId.of(command.id()))
                .orElseThrow(() -> new RuntimeException("Principal not found: " + command.id()));

        if (principal.getStatus() != PrincipalStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Principal must be ACTIVE to suspend. Current status: " + principal.getStatus());
        }

        principal.setStatus(PrincipalStatus.SUSPENDED);

        if (principal instanceof HumanPrincipalEntity) {
            humanPrincipalRepository.save((HumanPrincipalEntity) principal);
        } else if (principal instanceof ServicePrincipalEntity) {
            servicePrincipalRepository.save((ServicePrincipalEntity) principal);
        } else if (principal instanceof SystemPrincipalEntity) {
            systemPrincipalRepository.save((SystemPrincipalEntity) principal);
        } else if (principal instanceof DevicePrincipalEntity) {
            devicePrincipalRepository.save((DevicePrincipalEntity) principal);
        }

        // Disable in Keycloak
        try {
            if (principal instanceof HumanPrincipalEntity) {
                // Disable Keycloak user for human principals
                HumanPrincipalEntity humanPrincipal = (HumanPrincipalEntity) principal;
                if (humanPrincipal.getKeycloakUserId() != null && !humanPrincipal.getKeycloakUserId().isBlank()) {
                    User keycloakUser = User.builder()
                            .id(humanPrincipal.getBusinessId().toString())
                            .username(humanPrincipal.getUsername())
                            .email(humanPrincipal.getEmail())
                            .firstName(humanPrincipal.getFirstName())
                            .lastName(humanPrincipal.getLastName())
                            .enabled(false) // Disable the user
                            .emailVerified(humanPrincipal.getEmailVerified())
                            .build();
                    keycloakService.updateUser(humanPrincipal.getKeycloakUserId(), keycloakUser);
                    // TODO: Revoke all active sessions in Keycloak (requires additional KeycloakService method)
                }
            } else if (principal instanceof ServicePrincipalEntity) {
                // Disable Keycloak client for service principals
                ServicePrincipalEntity servicePrincipal = (ServicePrincipalEntity) principal;
                if (servicePrincipal.getKeycloakClientId() != null && !servicePrincipal.getKeycloakClientId().isBlank()) {
                    keycloakService.updateClient(servicePrincipal.getKeycloakClientId(), false);
                }
            } else if (principal instanceof SystemPrincipalEntity) {
                // Disable Keycloak client for system principals
                SystemPrincipalEntity systemPrincipal = (SystemPrincipalEntity) principal;
                if (systemPrincipal.getKeycloakClientId() != null && !systemPrincipal.getKeycloakClientId().isBlank()) {
                    keycloakService.updateClient(systemPrincipal.getKeycloakClientId(), false);
                }
            } else if (principal instanceof DevicePrincipalEntity) {
                // Disable Keycloak client for device principals
                DevicePrincipalEntity devicePrincipal = (DevicePrincipalEntity) principal;
                if (devicePrincipal.getKeycloakClientId() != null && !devicePrincipal.getKeycloakClientId().isBlank()) {
                    keycloakService.updateClient(devicePrincipal.getKeycloakClientId(), false);
                }
            }
        } catch (Exception e) {
            // Log error but don't fail the transaction - suspension should succeed even if Keycloak sync fails
            logger.error("Failed to disable principal in Keycloak for principal: " + principal.getBusinessId(), e);
        }


        PrincipalSuspendedEvent event = principalEventMapper.toPrincipalSuspendedEvent(principal, command);

//        RoutingKey routingKey = new RoutingKey(PRINCIPAL_SUSPENDED_KEY, NO_DESC, "", "");
//        // TODO: Pass event payload when event publisher supports it
//        eventPublisher.enqueue(IAM_PRINCIPAL_EXCHANGE, routingKey, null, Collections.emptyMap());

        return new PrincipalSuspended(principal.getBusinessId().value());
    }

    /**
     * Deactivates a principal.
     *
     * @param command the deactivation command
     * @return the deactivation result
     */
    @Transactional
    public PrincipalDeactivated deactivatePrincipal(DeactivatePrincipalCommand command) {

        Principal principal = findPrincipalByBusinessId(PrincipalId.of(command.id()))
                .orElseThrow(() -> new RuntimeException("Principal not found: " + command.id()));

        if (principal.getStatus() != PrincipalStatus.ACTIVE && principal.getStatus() != PrincipalStatus.SUSPENDED) {
            throw new IllegalStateException(
                    "Principal must be ACTIVE or SUSPENDED to deactivate. Current status: " + principal.getStatus());
        }

        principal.setStatus(PrincipalStatus.INACTIVE);

        if (principal instanceof HumanPrincipalEntity) {
            humanPrincipalRepository.save((HumanPrincipalEntity) principal);
        } else if (principal instanceof ServicePrincipalEntity) {
            servicePrincipalRepository.save((ServicePrincipalEntity) principal);
        } else if (principal instanceof SystemPrincipalEntity) {
            systemPrincipalRepository.save((SystemPrincipalEntity) principal);
        } else if (principal instanceof DevicePrincipalEntity) {
            devicePrincipalRepository.save((DevicePrincipalEntity) principal);
        }

        try {
            if (principal instanceof HumanPrincipalEntity) {
                HumanPrincipalEntity humanPrincipal = (HumanPrincipalEntity) principal;
                if (humanPrincipal.getKeycloakUserId() != null && !humanPrincipal.getKeycloakUserId().isBlank()) {
                    User keycloakUser = User.builder()
                            .id(humanPrincipal.getBusinessId().toString())
                            .username(humanPrincipal.getUsername())
                            .email(humanPrincipal.getEmail())
                            .firstName(humanPrincipal.getFirstName())
                            .lastName(humanPrincipal.getLastName())
                            .enabled(false)
                            .emailVerified(humanPrincipal.getEmailVerified())
                            .build();
                    keycloakService.updateUser(humanPrincipal.getKeycloakUserId(), keycloakUser);
                    // TODO: Revoke all active sessions in Keycloak (requires additional KeycloakService method)
                }
            } else if (principal instanceof ServicePrincipalEntity) {
                ServicePrincipalEntity servicePrincipal = (ServicePrincipalEntity) principal;
                if (servicePrincipal.getKeycloakClientId() != null && !servicePrincipal.getKeycloakClientId().isBlank()) {
                    keycloakService.updateClient(servicePrincipal.getKeycloakClientId(), false);
                }
            } else if (principal instanceof SystemPrincipalEntity) {
                SystemPrincipalEntity systemPrincipal = (SystemPrincipalEntity) principal;
                if (systemPrincipal.getKeycloakClientId() != null && !systemPrincipal.getKeycloakClientId().isBlank()) {
                    keycloakService.updateClient(systemPrincipal.getKeycloakClientId(), false);
                }
            } else if (principal instanceof DevicePrincipalEntity) {
                DevicePrincipalEntity devicePrincipal = (DevicePrincipalEntity) principal;
                if (devicePrincipal.getKeycloakClientId() != null && !devicePrincipal.getKeycloakClientId().isBlank()) {
                    keycloakService.updateClient(devicePrincipal.getKeycloakClientId(), false);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to disable principal in Keycloak for principal: " + principal.getBusinessId(), e);
        }

        // Publish event
        PrincipalDeactivatedEvent event = principalEventMapper.toPrincipalDeactivatedEvent(principal, command);

//        RoutingKey routingKey = new RoutingKey(PRINCIPAL_DEACTIVATED_KEY, NO_DESC, "", "");
//        // TODO: Pass event payload when event publisher supports it
//        eventPublisher.enqueue(IAM_PRINCIPAL_EXCHANGE, routingKey, null, Collections.emptyMap());

        return new PrincipalDeactivated(principal.getBusinessId().value());
    }

    @Transactional
    public PrincipalDeleted deletePrincipalGdpr(DeletePrincipalGdprCommand command) {

        Principal principal = findPrincipalByBusinessId(PrincipalId.of(command.id()))
                .orElseThrow(() -> new RuntimeException("Principal not found: " + command.id()));

        if (principal.getStatus() != PrincipalStatus.INACTIVE) {
            throw new IllegalStateException(
                    "Principal must be INACTIVE to delete per GDPR. Current status: " + principal.getStatus());
        }

        java.time.Instant inactiveThreshold = java.time.Instant.now().minus(java.time.Duration.ofDays(GDPR_RETENTION_DAYS));
        if (principal.getUpdatedAt() != null && principal.getUpdatedAt().isAfter(inactiveThreshold)) {
            throw new IllegalStateException(
                    "Principal must be INACTIVE for at least " + GDPR_RETENTION_DAYS + " days before GDPR deletion");
        }

        String auditReference = "aud-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        java.time.Instant deletedAt = java.time.Instant.now();

        String principalType = principal.getPrincipalType().name();

        String anonymizedUsername = "deleted_user_" + java.util.UUID.randomUUID();
        String anonymizedEmail = "deleted_" + deletedAt.toEpochMilli() + "@example.com";

        principal.setUsername(anonymizedUsername);
        principal.setEmail(anonymizedEmail);
        principal.setContextTags(null);
        principal.setStatus(PrincipalStatus.DELETED);

        if (principal instanceof HumanPrincipalEntity humanPrincipal) {
            humanPrincipal.setFirstName("Deleted");
            humanPrincipal.setLastName("User");
            humanPrincipal.setDisplayName("Deleted User");
            humanPrincipal.setPhone(null);
            humanPrincipal.setBio(null);
            humanPrincipal.setAvatarUrl(null);
            humanPrincipal.setPreferences(null);
            humanPrincipalRepository.save(humanPrincipal);

            // Delete from Keycloak
            try {
                if (humanPrincipal.getKeycloakUserId() != null && !humanPrincipal.getKeycloakUserId().isBlank()) {
                    keycloakService.deleteUser(humanPrincipal.getKeycloakUserId());
                }
            } catch (Exception e) {
                logger.error("Failed to delete user from Keycloak for principal: " + principal.getBusinessId(), e);
            }
        } else if (principal instanceof ServicePrincipalEntity servicePrincipal) {
            servicePrincipal.setServiceName(anonymizedUsername);
            servicePrincipal.setAllowedScopes(null);
            servicePrincipal.setApiKeyHash("deleted");
            servicePrincipalRepository.save(servicePrincipal);

            // Delete from Keycloak
            try {
                if (servicePrincipal.getKeycloakClientId() != null && !servicePrincipal.getKeycloakClientId().isBlank()) {
                    keycloakService.deleteClient(servicePrincipal.getKeycloakClientId());
                }
            } catch (Exception e) {
                logger.error("Failed to delete client from Keycloak for principal: " + principal.getBusinessId(), e);
            }
        } else if (principal instanceof SystemPrincipalEntity systemPrincipal) {
            systemPrincipal.setSystemIdentifier(anonymizedUsername);
            systemPrincipal.setCertificateThumbprint(null);
            systemPrincipal.setAllowedOperations(null);
            systemPrincipalRepository.save(systemPrincipal);

            try {
                if (systemPrincipal.getKeycloakClientId() != null && !systemPrincipal.getKeycloakClientId().isBlank()) {
                    keycloakService.deleteClient(systemPrincipal.getKeycloakClientId());
                }
            } catch (Exception e) {
                logger.error("Failed to delete client from Keycloak for principal: " + principal.getBusinessId(), e);
            }
        } else if (principal instanceof DevicePrincipalEntity devicePrincipal) {
            devicePrincipal.setDeviceIdentifier(anonymizedUsername);
            devicePrincipal.setManufacturer(null);
            devicePrincipal.setModel(null);
            devicePrincipalRepository.save(devicePrincipal);

            try {
                if (devicePrincipal.getKeycloakClientId() != null && !devicePrincipal.getKeycloakClientId().isBlank()) {
                    keycloakService.deleteClient(devicePrincipal.getKeycloakClientId());
                }
            } catch (Exception e) {
                logger.error("Failed to delete client from Keycloak for principal: " + principal.getBusinessId(), e);
            }
        }

        PrincipalDeletedEvent event = principalEventMapper.toPrincipalDeletedEvent(
                principal, command, principalType, auditReference, deletedAt);

//        RoutingKey routingKey = new RoutingKey(PRINCIPAL_DELETED_KEY, NO_DESC, "", "");
//        eventPublisher.enqueue(IAM_PRINCIPAL_EXCHANGE, routingKey, null, Collections.emptyMap());

        return new PrincipalDeleted(principal.getBusinessId().value(), true, auditReference, deletedAt);
    }

    /**
     * Searches principals with filters.
     *
     * @param query the search query
     * @return the search result
     */
    @Transactional(readOnly = true)
    public SearchPrincipalsResult searchPrincipals(SearchPrincipalsQuery query) {
        // Page is 1-indexed from API, but Spring Data uses 0-indexed
        int pageIndex = Math.max(0, query.page() - 1);
        int pageSize = Math.min(Math.max(1, query.size()), 100); // Limit to 100

        PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<SearchPrincipalsResult.PrincipalItem> items = new ArrayList<>();
        long total = 0;

        // If no specific principal type is requested, or HUMAN is requested, search human principals
        if (query.principalType() == null || query.principalType() == PrincipalType.HUMAN) {
            Page<HumanPrincipalEntity> humanPage = humanPrincipalRepository.searchPrincipals(
                    query.search(),
                    query.status(),
                    query.tenantId(),
                    pageRequest
            );

            for (HumanPrincipalEntity entity : humanPage.getContent()) {
                items.add(new SearchPrincipalsResult.PrincipalItem(
                        entity.getBusinessId().value(),
                        entity.getUsername(),
                        entity.getEmail(),
                        entity.getPrincipalType().name(),
                        entity.getStatus().name(),
                        entity.getDefaultTenantId(),
                        entity.getLastLoginAt(),
                        entity.getCreatedAt()
                ));
            }
            total = humanPage.getTotalElements();
        }

        return new SearchPrincipalsResult(items, total, query.page(), pageSize);
    }

    /**
     * Gets principal details by ID.
     *
     * @param principalId the principal ID
     * @return the principal details
     */
    @Transactional(readOnly = true)
    public PrincipalDetails getPrincipalDetails(PrincipalId principalId) {
        Principal principal = findPrincipalByBusinessId(principalId)
                .orElseThrow(() -> new RuntimeException("Principal not found: " + principalId));

        // Human principal specific fields
        Boolean emailVerified = null;
        Boolean mfaEnabled = null;
        java.time.Instant lastLoginAt = null;
        String displayName = null;
        String firstName = null;
        String lastName = null;

        // Service principal specific fields
        String serviceName = null;
        List<String> allowedScopes = null;
        java.time.LocalDate credentialRotationDate = null;

        // System principal specific fields
        String systemIdentifier = null;
        String integrationType = null;
        List<String> allowedOperations = null;

        // Device principal specific fields
        String deviceIdentifier = null;
        String deviceType = null;
        String manufacturer = null;
        String model = null;

        if (principal instanceof HumanPrincipalEntity human) {
            emailVerified = human.getEmailVerified();
            mfaEnabled = human.getMfaEnabled();
            lastLoginAt = human.getLastLoginAt();
            displayName = human.getDisplayName();
            firstName = human.getFirstName();
            lastName = human.getLastName();
        } else if (principal instanceof ServicePrincipalEntity service) {
            serviceName = service.getServiceName();
            allowedScopes = service.getAllowedScopes();
            credentialRotationDate = service.getCredentialRotationDate();
        } else if (principal instanceof SystemPrincipalEntity system) {
            systemIdentifier = system.getSystemIdentifier();
            integrationType = system.getIntegrationType() != null ? system.getIntegrationType().name() : null;
            allowedOperations = system.getAllowedOperations();
        } else if (principal instanceof DevicePrincipalEntity device) {
            deviceIdentifier = device.getDeviceIdentifier();
            deviceType = device.getDeviceType() != null ? device.getDeviceType().name() : null;
            manufacturer = device.getManufacturer();
            model = device.getModel();
        }

        return new PrincipalDetails(
                principal.getBusinessId().value(),
                principal.getPrincipalType().name(),
                principal.getUsername(),
                principal.getEmail(),
                principal.getStatus().name(),
                principal.getDefaultTenantId(),
                principal.getCreatedAt(),
                principal.getUpdatedAt(),
                principal.getContextTags(),
                emailVerified,
                mfaEnabled,
                lastLoginAt,
                displayName,
                firstName,
                lastName,
                serviceName,
                allowedScopes,
                credentialRotationDate,
                systemIdentifier,
                integrationType,
                allowedOperations,
                deviceIdentifier,
                deviceType,
                manufacturer,
                model
        );
    }

    /**
     * Updates common attributes on a principal.
     *
     * @param command the update command
     * @return the update result
     */
    @Transactional
    public CommonAttributesUpdated updateCommonAttributes(UpdateCommonAttributesCommand command) {
        // Find principal by ID
        Principal principal = findPrincipalByBusinessId(PrincipalId.of(command.id()))
                .orElseThrow(() -> new RuntimeException("Principal not found: " + command.id()));

        // Update context_tags
        principal.setContextTags(command.contextTags());

        // Save based on principal type
        if (principal instanceof HumanPrincipalEntity) {
            humanPrincipalRepository.save((HumanPrincipalEntity) principal);
        } else if (principal instanceof ServicePrincipalEntity) {
            servicePrincipalRepository.save((ServicePrincipalEntity) principal);
        } else if (principal instanceof SystemPrincipalEntity) {
            systemPrincipalRepository.save((SystemPrincipalEntity) principal);
        } else if (principal instanceof DevicePrincipalEntity) {
            devicePrincipalRepository.save((DevicePrincipalEntity) principal);
        }

        return new CommonAttributesUpdated(principal.getBusinessId().value());
    }

    /**
     * Gets credential status for a service or system principal.
     *
     * @param principalId the principal ID
     * @return the credential status
     */
    @Transactional(readOnly = true)
    public CredentialStatus getCredentialStatus(PrincipalId principalId) {
        Principal principal = findPrincipalByBusinessId(principalId)
                .orElseThrow(() -> new RuntimeException("Principal not found: " + principalId));

        if (principal instanceof ServicePrincipalEntity servicePrincipal) {
            return servicePrincipalMapper.toCredentialStatus(servicePrincipal);
        } else if (principal instanceof SystemPrincipalEntity systemPrincipal) {
            return systemPrincipalMapper.toCredentialStatus(systemPrincipal);
        }

        throw new IllegalArgumentException(
                "Credential status is only available for SERVICE or SYSTEM principals. Type: " + principal.getPrincipalType());
    }

    /**
     * Lists tenant memberships for a principal.
     *
     * @param id the principal ID
     * @param page page number (1-indexed)
     * @param size page size
     * @return the paginated list of tenant memberships
     */
//TODO: to be moved to iam.principal.tenants module
    //    @Transactional(readOnly = true)
//    public ListTenantMembershipsResult listTenantMemberships(java.util.UUID id, int page, int size) {
//        // Validate principal exists
//        Principal principal = findPrincipalById(id)
//                .orElseThrow(() -> new RuntimeException("Principal not found: " + id));
//
//        // Get all memberships for the principal
//        List<PrincipalTenantMembershipEntity> memberships = membershipRepository.findByPrincipalId(id);
//
//        // Get primary tenant ID for comparison
//        java.util.UUID defaultTenantId = principal.getPrimaryTenantId();
//
//        // Convert to items
//        List<TenantMembershipItem> allItems = memberships.stream()
//                .map(m -> new TenantMembershipItem(
//                        m.getId(),
//                        m.getBusinessId(),
//                        m.getTenantId(),
//                        m.getValidFrom(),
//                        m.getValidTo(),
//                        m.getStatus().name(),
//                        m.getTenantId().equals(defaultTenantId)
//                ))
//                .toList();
//
//        // Apply pagination
//        int pageIndex = Math.max(0, page - 1);
//        int pageSize = Math.min(Math.max(1, size), 100);
//        int fromIndex = pageIndex * pageSize;
//        int toIndex = Math.min(fromIndex + pageSize, allItems.size());
//
//        List<TenantMembershipItem> pagedItems;
//        if (fromIndex >= allItems.size()) {
//            pagedItems = List.of();
//        } else {
//            pagedItems = allItems.subList(fromIndex, toIndex);
//        }
//
//        return new ListTenantMembershipsResult(pagedItems, allItems.size(), page, pageSize);
//    }

    /**
     * Adds a tenant membership to a principal.
     *
     * @param command the add command
     * @return the created membership
     */
//    TODO: to be moved to iam.principal.tenants module
//    @Transactional
//    public TenantMembershipAdded addTenantMembership(AddTenantMembershipCommand command) {
//        // Validate principal exists and is ACTIVE
//        Principal principal = findPrincipalById(command.id())
//                .orElseThrow(() -> new RuntimeException("Principal not found: " + command.id()));
//
//        if (principal.getStatus() != PrincipalStatus.ACTIVE) {
//            throw new IllegalStateException("Principal is not ACTIVE: " + principal.getStatus());
//        }
//
//        // Check for existing ACTIVE membership
//        var existingMembership = membershipRepository.findByPrincipalIdAndTenantIdAndStatus(
//                command.id(), command.tenantId(), MembershipStatus.ACTIVE);
//        if (existingMembership.isPresent()) {
//            throw new IllegalStateException("Active membership already exists for this principal and tenant");
//        }
//
//        // Create new membership
//        PrincipalTenantMembershipEntity membership = new PrincipalTenantMembershipEntity();
//        membership.setPrincipalId(command.id());
//        membership.setPrincipalType(principal.getPrincipalType());
//        membership.setValidFrom(command.validFrom() != null ? command.validFrom() : java.time.LocalDate.now());
//        membership.setValidTo(command.validTo());
//        membership.setStatus(MembershipStatus.ACTIVE);
//        membership.setInvitedBy(command.invitedBy());
//        if (command.invitedBy() != null) {
//            // Try to determine the inviter's principal type
//            var inviter = findPrincipalById(command.invitedBy());
//            if (inviter.isPresent()) {
//                membership.setInvitedByType(inviter.get().getPrincipalType());
//            }
//        }
//
//        PrincipalTenantMembershipEntity savedMembership = membershipRepository.save(membership);
//
//        return new TenantMembershipAdded(
//                savedMembership.getId(),
//                savedMembership.getBusinessId(),
//                savedMembership.getTenantId(),
//                savedMembership.getValidFrom(),
//                savedMembership.getValidTo(),
//                savedMembership.getStatus().name(),
//                savedMembership.getInvitedBy(),
//                savedMembership.getCreatedAt()
//        );
//    }

    /**
     * Removes a tenant membership from a principal.
     *
     * @param command the remove command
     */
    // TODO: to be moved to iam.principal.tenants module
//    @Transactional
//    public void removeTenantMembership(RemoveTenantMembershipCommand command) {
//        // Validate principal exists
//        Principal principal = findPrincipalById(command.id())
//                .orElseThrow(() -> new RuntimeException("Principal not found: " + command.id()));
//
//        // Cannot remove primary tenant membership
//        if (command.tenantId().equals(principal.getPrimaryTenantId())) {
//            throw new IllegalStateException("Cannot remove primary tenant membership. Change primary tenant first.");
//        }
//
//        // Find the membership
//        PrincipalTenantMembershipEntity membership = membershipRepository
//                .findByPrincipalIdAndTenantIdAndStatus(command.id(), command.tenantId(), MembershipStatus.ACTIVE)
//                .orElseThrow(() -> new RuntimeException("Active membership not found for principal and tenant"));
//
//        // Update membership to EXPIRED
//        membership.setStatus(MembershipStatus.EXPIRED);
//        if (membership.getValidTo() == null) {
//            membership.setValidTo(java.time.LocalDate.now());
//        }
//
//        membershipRepository.save(membership);
//    }
    @Transactional
    public HeartbeatUpdated updateHeartbeat(UpdateHeartbeatCommand command) {

        DevicePrincipalEntity device = devicePrincipalRepository.findByBusinessId(PrincipalId.of(command.id()))
                .orElseThrow(() -> new RuntimeException("Device principal not found: " + command.id()));

        java.time.Instant now = java.time.Instant.now();
        device.setLastHeartbeatAt(now);

        if (command.firmwareVersion() != null) {
            device.setFirmwareVersion(command.firmwareVersion());
        }
        if (command.locationInfo() != null) {
            device.setLocationInfo(command.locationInfo());
        }

        devicePrincipalRepository.save(device);

        return new HeartbeatUpdated(device.getBusinessId().value(), now);
    }

    // TODO: to be moved to iam.tenant module
//    @Transactional(readOnly = true)
//    public CrossTenantSearchResult searchPrincipalsCrossTenant(CrossTenantSearchQuery query) {
//        List<CrossTenantPrincipalItem> allItems = new ArrayList<>();
//
//        // Search across all principal types
//        PrincipalStatus statusFilter = null;
//        if (query.status() != null && !query.status().isBlank()) {
//            try {
//                statusFilter = PrincipalStatus.valueOf(query.status());
//            } catch (IllegalArgumentException e) {
//                // Ignore invalid status
//            }
//        }
//
//        PrincipalType typeFilter = null;
//        if (query.principalType() != null && !query.principalType().isBlank()) {
//            try {
//                typeFilter = PrincipalType.valueOf(query.principalType());
//            } catch (IllegalArgumentException e) {
//                // Ignore invalid type
//            }
//        }
//
//        // Search human principals
//        if (typeFilter == null || typeFilter == PrincipalType.HUMAN) {
//            for (HumanPrincipalEntity p : humanPrincipalRepository.findAll()) {
//                if (matchesFilter(p, query.search(), statusFilter)) {
//                    allItems.add(new CrossTenantPrincipalItem(
//                            p.getBusinessId(), p.getPrincipalType().name(),
//                            p.getUsername(), p.getEmail(), p.getStatus().name(), p.getPrimaryTenantId()));
//                }
//            }
//        }
//
//        // Search service principals
//        if (typeFilter == null || typeFilter == PrincipalType.SERVICE) {
//            for (ServicePrincipalEntity p : servicePrincipalRepository.findAll()) {
//                if (matchesFilter(p, query.search(), statusFilter)) {
//                    allItems.add(new CrossTenantPrincipalItem(
//                            p.getBusinessId(), p.getPrincipalType().name(),
//                            p.getUsername(), p.getEmail(), p.getStatus().name(), p.getPrimaryTenantId()));
//                }
//            }
//        }
//
//        // Search system principals
//        if (typeFilter == null || typeFilter == PrincipalType.SYSTEM) {
//            for (SystemPrincipalEntity p : systemPrincipalRepository.findAll()) {
//                if (matchesFilter(p, query.search(), statusFilter)) {
//                    allItems.add(new CrossTenantPrincipalItem(
//                            p.getBusinessId(), p.getPrincipalType().name(),
//                            p.getUsername(), p.getEmail(), p.getStatus().name(), p.getPrimaryTenantId()));
//                }
//            }
//        }
//
//        // Search device principals
//        if (typeFilter == null || typeFilter == PrincipalType.DEVICE) {
//            for (DevicePrincipalEntity p : devicePrincipalRepository.findAll()) {
//                if (matchesFilter(p, query.search(), statusFilter)) {
//                    allItems.add(new CrossTenantPrincipalItem(
//                            p.getPrincipalId(), p.getPrincipalType().name(),
//                            p.getUsername(), p.getEmail(), p.getStatus().name(), p.getPrimaryTenantId()));
//                }
//            }
//        }
//
//        // Apply pagination
//        int pageIndex = Math.max(0, query.page() - 1);
//        int pageSize = Math.min(Math.max(1, query.size()), 100);
//        int fromIndex = pageIndex * pageSize;
//        int toIndex = Math.min(fromIndex + pageSize, allItems.size());
//
//        List<CrossTenantPrincipalItem> pagedItems;
//        if (fromIndex >= allItems.size()) {
//            pagedItems = List.of();
//        } else {
//            pagedItems = allItems.subList(fromIndex, toIndex);
//        }
//
//        return new CrossTenantSearchResult(pagedItems, allItems.size(), query.page(), pageSize);
//    }

    private boolean matchesFilter(Principal principal, String search, PrincipalStatus statusFilter) {
        if (statusFilter != null && principal.getStatus() != statusFilter) {
            return false;
        }
        if (search != null && !search.isBlank()) {
            String lowerSearch = search.toLowerCase();
            boolean matchesUsername = principal.getUsername() != null &&
                    principal.getUsername().toLowerCase().contains(lowerSearch);
            boolean matchesEmail = principal.getEmail() != null &&
                    principal.getEmail().toLowerCase().contains(lowerSearch);
            return matchesUsername || matchesEmail;
        }
        return true;
    }
}
