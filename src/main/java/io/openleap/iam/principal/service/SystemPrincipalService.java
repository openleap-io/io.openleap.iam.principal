package io.openleap.iam.principal.service;

import io.openleap.common.messaging.event.EventPublisher;
import io.openleap.iam.principal.domain.dto.CreateSystemPrincipalCommand;
import io.openleap.iam.principal.domain.dto.SystemPrincipalCreated;
import io.openleap.iam.principal.domain.entity.*;
import io.openleap.iam.principal.domain.event.SystemPrincipalCreatedEvent;
import io.openleap.iam.principal.domain.mapper.SystemPrincipalMapper;
import io.openleap.iam.principal.exception.SystemIdentifierAlreadyExistsException;
import io.openleap.iam.principal.exception.TenantNotFoundException;
import io.openleap.iam.principal.exception.UsernameAlreadyExistsException;
import io.openleap.iam.principal.repository.SystemPrincipalRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class SystemPrincipalService {

    private final SystemPrincipalRepository systemPrincipalRepository;
    private final TenantService tenantService;
    private final EventPublisher eventPublisher;
    private final SystemPrincipalMapper systemPrincipalMapper;

    public SystemPrincipalService(
            SystemPrincipalRepository systemPrincipalRepository,
            TenantService tenantService,
            EventPublisher eventPublisher,
            SystemPrincipalMapper systemPrincipalMapper) {
        this.systemPrincipalRepository = systemPrincipalRepository;
        this.tenantService = tenantService;
        this.eventPublisher = eventPublisher;
        this.systemPrincipalMapper = systemPrincipalMapper;
    }

    @Transactional
    public SystemPrincipalCreated createSystemPrincipal(CreateSystemPrincipalCommand command) {
        if (systemPrincipalRepository.existsBySystemIdentifier(command.systemIdentifier())) {
            throw new SystemIdentifierAlreadyExistsException(command.systemIdentifier());
        }

        String username = command.systemIdentifier().toLowerCase();
        if (systemPrincipalRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(username);
        }

        if (!tenantService.tenantExists(command.defaultTenantId())) {
            throw new TenantNotFoundException(command.defaultTenantId());
        }

        if (command.certificateThumbprint() == null || command.certificateThumbprint().isBlank()) {
            throw new IllegalArgumentException("Certificate thumbprint is required for system principal");
        }

        SystemPrincipalEntity principal = createSystemPrincipalEntity(command, username);

        // Save principal
        principal = systemPrincipalRepository.save(principal);

        // Create PrincipalTenantMembership for primary tenant
        PrincipalTenantMembershipEntity membership = new PrincipalTenantMembershipEntity();
        membership.setPrincipalId(principal.getBusinessId().value());
        membership.setPrincipalType(PrincipalType.SYSTEM);
        membership.setTenantId(command.defaultTenantId());
        membership.setValidFrom(LocalDate.now());
        membership.setStatus(MembershipStatus.ACTIVE);

        SystemPrincipalCreatedEvent event = systemPrincipalMapper.toSystemPrincipalCreatedEvent(principal);

//        TODO: Enable event publishing once consumers are ready
//        RoutingKey routingKey = new RoutingKey(SYSTEM_PRINCIPAL_CREATED_KEY, NO_DESC, "", "");
//        eventPublisher.enqueue(IAM_PRINCIPAL_EXCHANGE, routingKey, null, Collections.emptyMap());

        return systemPrincipalMapper.toSystemPrincipalCreated(principal);
    }

    private static @NonNull SystemPrincipalEntity createSystemPrincipalEntity(CreateSystemPrincipalCommand command, String username) {
        SystemPrincipalEntity principal = new SystemPrincipalEntity();
        principal.setBusinessId(PrincipalId.create());
        principal.setUsername(username);
        principal.setSystemIdentifier(command.systemIdentifier());
        principal.setIntegrationType(command.integrationType());
        principal.setDefaultTenantId(command.defaultTenantId());
        principal.setStatus(PrincipalStatus.ACTIVE); // System principals are ACTIVE immediately
        principal.setSyncStatus(SyncStatus.SYNCED); // No Keycloak sync needed for mTLS-only systems
        principal.setContextTags(command.contextTags());
        principal.setCertificateThumbprint(command.certificateThumbprint());
        principal.setAllowedOperations(command.allowedOperations());
        return principal;
    }
}
