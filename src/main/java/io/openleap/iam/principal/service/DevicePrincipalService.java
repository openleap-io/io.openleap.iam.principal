package io.openleap.iam.principal.service;

import io.openleap.common.messaging.event.EventPublisher;
import io.openleap.iam.principal.domain.dto.CreateDevicePrincipalCommand;
import io.openleap.iam.principal.domain.dto.DevicePrincipalCreated;
import io.openleap.iam.principal.domain.entity.DevicePrincipalEntity;
import io.openleap.iam.principal.domain.entity.PrincipalId;
import io.openleap.iam.principal.domain.entity.PrincipalStatus;
import io.openleap.iam.principal.domain.entity.SyncStatus;
import io.openleap.iam.principal.domain.event.DevicePrincipalCreatedEvent;
import io.openleap.iam.principal.domain.mapper.DevicePrincipalMapper;
import io.openleap.iam.principal.exception.DeviceIdentifierAlreadyExistsException;
import io.openleap.iam.principal.exception.UsernameAlreadyExistsException;
import io.openleap.iam.principal.repository.DevicePrincipalRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DevicePrincipalService {

    private final DevicePrincipalRepository devicePrincipalRepository;
    private final TenantService tenantService;
    private final EventPublisher eventPublisher;
    private final DevicePrincipalMapper devicePrincipalMapper;

    private static final String IAM_PRINCIPAL_EXCHANGE = "iam.principal.events";
    private static final String DEVICE_PRINCIPAL_CREATED_KEY = "iam.principal.device_principal.created";
    private static final String NO_DESC = "nodesc";

    public DevicePrincipalService(
            DevicePrincipalRepository devicePrincipalRepository,
            TenantService tenantService,
            EventPublisher eventPublisher,
            DevicePrincipalMapper devicePrincipalMapper) {
        this.devicePrincipalRepository = devicePrincipalRepository;
        this.tenantService = tenantService;
        this.eventPublisher = eventPublisher;
        this.devicePrincipalMapper = devicePrincipalMapper;
    }

    @Transactional
    public DevicePrincipalCreated createDevicePrincipal(CreateDevicePrincipalCommand command) {

        if (devicePrincipalRepository.existsByDeviceIdentifier(command.deviceIdentifier())) {
            throw new DeviceIdentifierAlreadyExistsException(command.deviceIdentifier());
        }

        String username = command.deviceIdentifier().toLowerCase();
        if (devicePrincipalRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(username);
        }

        if (command.certificateThumbprint() == null || command.certificateThumbprint().isBlank()) {
            throw new IllegalArgumentException("Certificate thumbprint is required for device principal");
        }

        DevicePrincipalEntity principal = createDevicePrincipalEntity(command, username);

        principal = devicePrincipalRepository.save(principal);


        DevicePrincipalCreatedEvent event = devicePrincipalMapper.toDevicePrincipalCreatedEvent(principal);

//        RoutingKey routingKey = new RoutingKey(DEVICE_PRINCIPAL_CREATED_KEY, NO_DESC, "", "");
//        eventPublisher.enqueue(IAM_PRINCIPAL_EXCHANGE, routingKey, null, Collections.emptyMap());

        return devicePrincipalMapper.toDevicePrincipalCreated(principal);
    }

    private static @NonNull DevicePrincipalEntity createDevicePrincipalEntity(CreateDevicePrincipalCommand command, String username) {
        DevicePrincipalEntity principal = new DevicePrincipalEntity();
        principal.setBusinessId(PrincipalId.create());
        principal.setUsername(username);
        principal.setDeviceIdentifier(command.deviceIdentifier());
        principal.setDeviceType(command.deviceType());
        principal.setDefaultTenantId(command.defaultTenantId());
        principal.setStatus(PrincipalStatus.ACTIVE); // Device principals are ACTIVE immediately
        principal.setSyncStatus(SyncStatus.SYNCED); // No Keycloak sync needed for mTLS-only devices
        principal.setContextTags(command.contextTags());
        principal.setCertificateThumbprint(command.certificateThumbprint());
        principal.setManufacturer(command.manufacturer());
        principal.setModel(command.model());
        principal.setFirmwareVersion(command.firmwareVersion());
        principal.setLocationInfo(command.locationInfo());
        return principal;
    }
}
