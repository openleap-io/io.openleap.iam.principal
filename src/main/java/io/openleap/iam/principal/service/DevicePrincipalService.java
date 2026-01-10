package io.openleap.iam.principal.service;

import io.openleap.iam.principal.domain.dto.CreateDevicePrincipalCommand;
import io.openleap.iam.principal.domain.dto.DevicePrincipalCreated;
import io.openleap.iam.principal.domain.entity.*;
import io.openleap.iam.principal.domain.event.DevicePrincipalCreatedEvent;
import io.openleap.iam.principal.exception.DeviceIdentifierAlreadyExistsException;
import io.openleap.iam.principal.exception.TenantNotFoundException;
import io.openleap.iam.principal.exception.UsernameAlreadyExistsException;
import io.openleap.iam.principal.repository.DevicePrincipalRepository;
import io.openleap.iam.principal.repository.PrincipalTenantMembershipRepository;
import io.openleap.starter.core.messaging.RoutingKey;
import io.openleap.starter.core.messaging.event.EventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;

@Service
public class DevicePrincipalService {

    private final DevicePrincipalRepository devicePrincipalRepository;
    private final PrincipalTenantMembershipRepository membershipRepository;
    private final TenantService tenantService;
    private final EventPublisher eventPublisher;

    private static final String IAM_PRINCIPAL_EXCHANGE = "iam.principal.events";
    private static final String DEVICE_PRINCIPAL_CREATED_KEY = "iam.principal.device_principal.created";
    private static final String NO_DESC = "nodesc";

    public DevicePrincipalService(
            DevicePrincipalRepository devicePrincipalRepository,
            PrincipalTenantMembershipRepository membershipRepository,
            TenantService tenantService,
            EventPublisher eventPublisher) {
        this.devicePrincipalRepository = devicePrincipalRepository;
        this.membershipRepository = membershipRepository;
        this.tenantService = tenantService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public DevicePrincipalCreated createDevicePrincipal(CreateDevicePrincipalCommand command) {
        // Validate device identifier uniqueness
        if (devicePrincipalRepository.existsByDeviceIdentifier(command.deviceIdentifier())) {
            throw new DeviceIdentifierAlreadyExistsException(command.deviceIdentifier());
        }

        // Validate username uniqueness (device identifier becomes username)
        String username = command.deviceIdentifier().toLowerCase();
        if (devicePrincipalRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(username);
        }

        // Validate primary tenant exists
        if (!tenantService.tenantExists(command.primaryTenantId())) {
            throw new TenantNotFoundException(command.primaryTenantId());
        }

        // Validate certificate thumbprint is provided
        if (command.certificateThumbprint() == null || command.certificateThumbprint().isBlank()) {
            throw new IllegalArgumentException("Certificate thumbprint is required for device principal");
        }

        // Create DevicePrincipalEntity
        DevicePrincipalEntity principal = new DevicePrincipalEntity();
        principal.setUsername(username);
        principal.setDeviceIdentifier(command.deviceIdentifier());
        principal.setDeviceType(command.deviceType());
        principal.setPrimaryTenantId(command.primaryTenantId());
        principal.setStatus(PrincipalStatus.ACTIVE); // Device principals are ACTIVE immediately
        principal.setSyncStatus(SyncStatus.SYNCED); // No Keycloak sync needed for mTLS-only devices
        principal.setContextTags(command.contextTags());
        principal.setCertificateThumbprint(command.certificateThumbprint());
        principal.setManufacturer(command.manufacturer());
        principal.setModel(command.model());
        principal.setFirmwareVersion(command.firmwareVersion());
        principal.setLocationInfo(command.locationInfo());

        // Save principal
        principal = devicePrincipalRepository.save(principal);

        // Create PrincipalTenantMembership for primary tenant
        PrincipalTenantMembershipEntity membership = new PrincipalTenantMembershipEntity();
        membership.setPrincipalId(principal.getPrincipalId());
        membership.setPrincipalType(PrincipalType.DEVICE);
        membership.setTenantId(command.primaryTenantId());
        membership.setValidFrom(LocalDate.now());
        membership.setStatus(MembershipStatus.ACTIVE);

        membershipRepository.save(membership);

        // Note: Keycloak client creation is optional and can be added if OAuth2 is needed
        // For mTLS-only devices, no Keycloak sync is required

        // Publish device principal created event
        DevicePrincipalCreatedEvent event = new DevicePrincipalCreatedEvent(
                principal.getPrincipalId(),
                principal.getPrincipalType().name(),
                principal.getUsername(),
                principal.getDeviceIdentifier(),
                principal.getDeviceType() != null ? principal.getDeviceType().name() : null,
                principal.getPrimaryTenantId(),
                principal.getStatus().name(),
                principal.getManufacturer(),
                principal.getModel(),
                principal.getFirmwareVersion(),
                principal.getCertificateThumbprint(),
                principal.getLocationInfo(),
                principal.getCreatedBy()
        );

        RoutingKey routingKey = new RoutingKey(DEVICE_PRINCIPAL_CREATED_KEY, NO_DESC, "", "");
        eventPublisher.enqueue(IAM_PRINCIPAL_EXCHANGE, routingKey, null, Collections.emptyMap());

        return new DevicePrincipalCreated(
                principal.getPrincipalId(),
                principal.getUsername(),
                principal.getDeviceIdentifier(),
                principal.getDeviceType(),
                principal.getManufacturer(),
                principal.getModel(),
                principal.getCertificateThumbprint()
        );
    }
}
