package io.openleap.iam.principal.service;

import io.openleap.iam.principal.domain.dto.ActivatePrincipalCommand;
import io.openleap.iam.principal.domain.dto.DeactivatePrincipalCommand;
import io.openleap.iam.principal.domain.dto.PrincipalActivated;
import io.openleap.iam.principal.domain.dto.PrincipalDeactivated;
import io.openleap.iam.principal.domain.dto.PrincipalSuspended;
import io.openleap.iam.principal.domain.dto.SuspendPrincipalCommand;
import io.openleap.iam.principal.domain.entity.*;
import io.openleap.iam.principal.domain.event.PrincipalActivatedEvent;
import io.openleap.iam.principal.domain.event.PrincipalDeactivatedEvent;
import io.openleap.iam.principal.domain.event.PrincipalSuspendedEvent;
import io.openleap.iam.principal.repository.DevicePrincipalRepository;
import io.openleap.iam.principal.repository.HumanPrincipalRepository;
import io.openleap.iam.principal.repository.PrincipalTenantMembershipRepository;
import io.openleap.iam.principal.repository.ServicePrincipalRepository;
import io.openleap.iam.principal.repository.SystemPrincipalRepository;
import io.openleap.iam.principal.service.keycloak.KeycloakService;
import io.openleap.iam.principal.service.keycloak.dto.User;
import io.openleap.starter.core.messaging.RoutingKey;
import io.openleap.starter.core.messaging.event.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
public class PrincipalService {

    private static final Logger logger = LoggerFactory.getLogger(PrincipalService.class);

    private final HumanPrincipalRepository humanPrincipalRepository;
    private final ServicePrincipalRepository servicePrincipalRepository;
    private final SystemPrincipalRepository systemPrincipalRepository;
    private final DevicePrincipalRepository devicePrincipalRepository;
    private final PrincipalTenantMembershipRepository membershipRepository;
    private final KeycloakService keycloakService;
    private final EventPublisher eventPublisher;

    private static final String IAM_PRINCIPAL_EXCHANGE = "iam.principal.events";
    private static final String PRINCIPAL_ACTIVATED_KEY = "iam.principal.principal.activated";
    private static final String PRINCIPAL_SUSPENDED_KEY = "iam.principal.principal.suspended";
    private static final String PRINCIPAL_DEACTIVATED_KEY = "iam.principal.principal.deactivated";
    private static final String NO_DESC = "nodesc";

    public PrincipalService(
            HumanPrincipalRepository humanPrincipalRepository,
            ServicePrincipalRepository servicePrincipalRepository,
            SystemPrincipalRepository systemPrincipalRepository,
            DevicePrincipalRepository devicePrincipalRepository,
            PrincipalTenantMembershipRepository membershipRepository,
            KeycloakService keycloakService,
            EventPublisher eventPublisher) {
        this.humanPrincipalRepository = humanPrincipalRepository;
        this.servicePrincipalRepository = servicePrincipalRepository;
        this.systemPrincipalRepository = systemPrincipalRepository;
        this.devicePrincipalRepository = devicePrincipalRepository;
        this.membershipRepository = membershipRepository;
        this.keycloakService = keycloakService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Finds a principal by ID across all principal types.
     * 
     * @param principalId the principal ID
     * @return the principal entity if found, empty otherwise
     */
    public Optional<Principal> findPrincipalById(java.util.UUID principalId) {
        // Try each repository in order
        Optional<HumanPrincipalEntity> human = humanPrincipalRepository.findById(principalId);
        if (human.isPresent()) {
            return Optional.of(human.get());
        }

        Optional<ServicePrincipalEntity> service = servicePrincipalRepository.findById(principalId);
        if (service.isPresent()) {
            return Optional.of(service.get());
        }

        Optional<SystemPrincipalEntity> system = systemPrincipalRepository.findById(principalId);
        if (system.isPresent()) {
            return Optional.of(system.get());
        }

        Optional<DevicePrincipalEntity> device = devicePrincipalRepository.findById(principalId);
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
        Principal principal = findPrincipalById(command.principalId())
                .orElseThrow(() -> new RuntimeException("Principal not found: " + command.principalId()));

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
                            .id(humanPrincipal.getPrincipalId().toString())
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
            logger.error("Failed to enable principal in Keycloak for principal: " + principal.getPrincipalId(), e);
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
                principal.getPrincipalId(),
                principal.getPrincipalType().name(),
                principal.getStatus().name(),
                activatedBy,
                activationMethod
        );

        RoutingKey routingKey = new RoutingKey(PRINCIPAL_ACTIVATED_KEY, NO_DESC, "", "");
        // TODO: Pass event payload when event publisher supports it
        eventPublisher.enqueue(IAM_PRINCIPAL_EXCHANGE, routingKey, null, Collections.emptyMap());

        return new PrincipalActivated(principal.getPrincipalId());
    }

    /**
     * Suspends a principal.
     * 
     * @param command the suspension command
     * @return the suspension result
     */
    @Transactional
    public PrincipalSuspended suspendPrincipal(SuspendPrincipalCommand command) {
        // Find principal by ID
        Principal principal = findPrincipalById(command.principalId())
                .orElseThrow(() -> new RuntimeException("Principal not found: " + command.principalId()));

        // Validate principal is ACTIVE
        if (principal.getStatus() != PrincipalStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Principal must be ACTIVE to suspend. Current status: " + principal.getStatus());
        }

        // Update status to SUSPENDED
        principal.setStatus(PrincipalStatus.SUSPENDED);

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

        // Disable in Keycloak
        try {
            if (principal instanceof HumanPrincipalEntity) {
                // Disable Keycloak user for human principals
                HumanPrincipalEntity humanPrincipal = (HumanPrincipalEntity) principal;
                if (humanPrincipal.getKeycloakUserId() != null && !humanPrincipal.getKeycloakUserId().isBlank()) {
                    User keycloakUser = User.builder()
                            .id(humanPrincipal.getPrincipalId().toString())
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
            logger.error("Failed to disable principal in Keycloak for principal: " + principal.getPrincipalId(), e);
        }

        // Update all PrincipalTenantMemberships to SUSPENDED
        var memberships = membershipRepository.findByPrincipalId(principal.getPrincipalId());
        for (var membership : memberships) {
            if (membership.getStatus() == MembershipStatus.ACTIVE) {
                membership.setStatus(MembershipStatus.SUSPENDED);
                membershipRepository.save(membership);
            }
        }

        // Publish event
        PrincipalSuspendedEvent event = new PrincipalSuspendedEvent(
                principal.getPrincipalId(),
                principal.getPrincipalType().name(),
                principal.getStatus().name(),
                command.reason(),
                command.incidentTicket()
        );

        RoutingKey routingKey = new RoutingKey(PRINCIPAL_SUSPENDED_KEY, NO_DESC, "", "");
        // TODO: Pass event payload when event publisher supports it
        eventPublisher.enqueue(IAM_PRINCIPAL_EXCHANGE, routingKey, null, Collections.emptyMap());

        return new PrincipalSuspended(principal.getPrincipalId());
    }

    /**
     * Deactivates a principal.
     *
     * @param command the deactivation command
     * @return the deactivation result
     */
    @Transactional
    public PrincipalDeactivated deactivatePrincipal(DeactivatePrincipalCommand command) {
        // Find principal by ID
        Principal principal = findPrincipalById(command.principalId())
                .orElseThrow(() -> new RuntimeException("Principal not found: " + command.principalId()));

        // Validate principal is ACTIVE or SUSPENDED
        if (principal.getStatus() != PrincipalStatus.ACTIVE && principal.getStatus() != PrincipalStatus.SUSPENDED) {
            throw new IllegalStateException(
                    "Principal must be ACTIVE or SUSPENDED to deactivate. Current status: " + principal.getStatus());
        }

        // Update status to INACTIVE
        principal.setStatus(PrincipalStatus.INACTIVE);

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

        // Disable in Keycloak and revoke sessions
        try {
            if (principal instanceof HumanPrincipalEntity) {
                HumanPrincipalEntity humanPrincipal = (HumanPrincipalEntity) principal;
                if (humanPrincipal.getKeycloakUserId() != null && !humanPrincipal.getKeycloakUserId().isBlank()) {
                    User keycloakUser = User.builder()
                            .id(humanPrincipal.getPrincipalId().toString())
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
            logger.error("Failed to disable principal in Keycloak for principal: " + principal.getPrincipalId(), e);
        }

        // Publish event
        PrincipalDeactivatedEvent event = new PrincipalDeactivatedEvent(
                principal.getPrincipalId(),
                principal.getPrincipalType().name(),
                principal.getStatus().name(),
                command.reason(),
                command.effectiveDate()
        );

        RoutingKey routingKey = new RoutingKey(PRINCIPAL_DEACTIVATED_KEY, NO_DESC, "", "");
        // TODO: Pass event payload when event publisher supports it
        eventPublisher.enqueue(IAM_PRINCIPAL_EXCHANGE, routingKey, null, Collections.emptyMap());

        return new PrincipalDeactivated(principal.getPrincipalId());
    }
}
