package io.openleap.iam.principal.service;

import io.openleap.iam.principal.domain.dto.CreateSystemPrincipalCommand;
import io.openleap.iam.principal.domain.dto.SystemPrincipalCreated;
import io.openleap.iam.principal.domain.entity.*;
import io.openleap.iam.principal.domain.event.SystemPrincipalCreatedEvent;
import io.openleap.iam.principal.exception.SystemIdentifierAlreadyExistsException;
import io.openleap.iam.principal.exception.TenantNotFoundException;
import io.openleap.iam.principal.exception.UsernameAlreadyExistsException;
import io.openleap.iam.principal.repository.PrincipalTenantMembershipRepository;
import io.openleap.iam.principal.repository.SystemPrincipalRepository;
import io.openleap.starter.core.messaging.RoutingKey;
import io.openleap.starter.core.messaging.event.EventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;

@Service
public class SystemPrincipalService {

    private final SystemPrincipalRepository systemPrincipalRepository;
    private final PrincipalTenantMembershipRepository membershipRepository;
    private final TenantService tenantService;
    private final EventPublisher eventPublisher;

    private static final String IAM_PRINCIPAL_EXCHANGE = "iam.principal.events";
    private static final String SYSTEM_PRINCIPAL_CREATED_KEY = "iam.principal.system_principal.created";
    private static final String NO_DESC = "nodesc";

    public SystemPrincipalService(
            SystemPrincipalRepository systemPrincipalRepository,
            PrincipalTenantMembershipRepository membershipRepository,
            TenantService tenantService,
            EventPublisher eventPublisher) {
        this.systemPrincipalRepository = systemPrincipalRepository;
        this.membershipRepository = membershipRepository;
        this.tenantService = tenantService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public SystemPrincipalCreated createSystemPrincipal(CreateSystemPrincipalCommand command) {
        // Validate system identifier uniqueness
        if (systemPrincipalRepository.existsBySystemIdentifier(command.systemIdentifier())) {
            throw new SystemIdentifierAlreadyExistsException(command.systemIdentifier());
        }

        // Validate username uniqueness (system identifier becomes username)
        String username = command.systemIdentifier().toLowerCase();
        if (systemPrincipalRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(username);
        }

        // Validate primary tenant exists
        if (!tenantService.tenantExists(command.primaryTenantId())) {
            throw new TenantNotFoundException(command.primaryTenantId());
        }

        // Validate certificate thumbprint is provided (BR-SYS-002: Must have certificate OR API key)
        if (command.certificateThumbprint() == null || command.certificateThumbprint().isBlank()) {
            throw new IllegalArgumentException("Certificate thumbprint is required for system principal");
        }

        // Create SystemPrincipalEntity
        SystemPrincipalEntity principal = new SystemPrincipalEntity();
        principal.setUsername(username);
        principal.setSystemIdentifier(command.systemIdentifier());
        principal.setIntegrationType(command.integrationType());
        principal.setPrimaryTenantId(command.primaryTenantId());
        principal.setStatus(PrincipalStatus.ACTIVE); // System principals are ACTIVE immediately
        principal.setSyncStatus(SyncStatus.SYNCED); // No Keycloak sync needed for mTLS-only systems
        principal.setContextTags(command.contextTags());
        principal.setCertificateThumbprint(command.certificateThumbprint());
        principal.setAllowedOperations(command.allowedOperations());

        // Save principal
        principal = systemPrincipalRepository.save(principal);

        // Create PrincipalTenantMembership for primary tenant
        PrincipalTenantMembershipEntity membership = new PrincipalTenantMembershipEntity();
        membership.setPrincipalId(principal.getPrincipalId());
        membership.setPrincipalType(PrincipalType.SYSTEM);
        membership.setTenantId(command.primaryTenantId());
        membership.setValidFrom(LocalDate.now());
        membership.setStatus(MembershipStatus.ACTIVE);

        membershipRepository.save(membership);

        // Publish system principal created event
        SystemPrincipalCreatedEvent event = new SystemPrincipalCreatedEvent(
                principal.getPrincipalId(),
                principal.getPrincipalType().name(),
                principal.getUsername(),
                principal.getSystemIdentifier(),
                principal.getIntegrationType() != null ? principal.getIntegrationType().name() : null,
                principal.getPrimaryTenantId(),
                principal.getStatus().name(),
                principal.getAllowedOperations(),
                principal.getCreatedBy()
        );

        RoutingKey routingKey = new RoutingKey(SYSTEM_PRINCIPAL_CREATED_KEY, NO_DESC, "", "");
        eventPublisher.enqueue(IAM_PRINCIPAL_EXCHANGE, routingKey, null, Collections.emptyMap());

        return new SystemPrincipalCreated(
                principal.getPrincipalId(),
                principal.getUsername(),
                principal.getSystemIdentifier(),
                principal.getIntegrationType() != null ? principal.getIntegrationType().name() : null,
                principal.getCertificateThumbprint(),
                principal.getAllowedOperations()
        );
    }
}
