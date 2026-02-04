package io.openleap.iam.principal.controller.mapper;

import io.openleap.iam.principal.controller.dto.ActivatePrincipalRequestDto;
import io.openleap.iam.principal.controller.dto.ActivatePrincipalResponseDto;
import io.openleap.iam.principal.controller.dto.DeactivatePrincipalRequestDto;
import io.openleap.iam.principal.controller.dto.DeactivatePrincipalResponseDto;
import io.openleap.iam.principal.controller.dto.DeletePrincipalGdprRequestDto;
import io.openleap.iam.principal.controller.dto.DeletePrincipalGdprResponseDto;
import io.openleap.iam.principal.controller.dto.AddTenantMembershipRequestDto;
import io.openleap.iam.principal.controller.dto.AddTenantMembershipResponseDto;
import io.openleap.iam.principal.controller.dto.CrossTenantSearchResponseDto;
import io.openleap.iam.principal.controller.dto.GetCredentialStatusResponseDto;
import io.openleap.iam.principal.controller.dto.GetPrincipalResponseDto;
import io.openleap.iam.principal.controller.dto.GetProfileResponseDto;
import io.openleap.iam.principal.controller.dto.ListTenantMembershipsResponseDto;
import io.openleap.iam.principal.controller.dto.SearchPrincipalsResponseDto;
import io.openleap.iam.principal.controller.dto.UpdateHeartbeatRequestDto;
import io.openleap.iam.principal.controller.dto.UpdateHeartbeatResponseDto;
import io.openleap.iam.principal.controller.dto.RotateCredentialsRequestDto;
import io.openleap.iam.principal.controller.dto.RotateCredentialsResponseDto;
import io.openleap.iam.principal.controller.dto.SuspendPrincipalRequestDto;
import io.openleap.iam.principal.controller.dto.SuspendPrincipalResponseDto;
import io.openleap.iam.principal.controller.dto.UpdateCommonAttributesRequestDto;
import io.openleap.iam.principal.controller.dto.UpdateCommonAttributesResponseDto;
import io.openleap.iam.principal.controller.dto.CreateDevicePrincipalRequestDto;
import io.openleap.iam.principal.controller.dto.CreateDevicePrincipalResponseDto;
import io.openleap.iam.principal.controller.dto.CreateHumanPrincipalRequestDto;
import io.openleap.iam.principal.controller.dto.CreateHumanPrincipalResponseDto;
import io.openleap.iam.principal.controller.dto.CreateServicePrincipalRequestDto;
import io.openleap.iam.principal.controller.dto.CreateServicePrincipalResponseDto;
import io.openleap.iam.principal.controller.dto.CreateSystemPrincipalRequestDto;
import io.openleap.iam.principal.controller.dto.CreateSystemPrincipalResponseDto;
import io.openleap.iam.principal.controller.dto.UpdateProfileRequestDto;
import io.openleap.iam.principal.controller.dto.UpdateProfileResponseDto;
import io.openleap.iam.principal.domain.dto.ActivatePrincipalCommand;
import io.openleap.iam.principal.domain.dto.AddTenantMembershipCommand;
import io.openleap.iam.principal.domain.dto.CommonAttributesUpdated;
import io.openleap.iam.principal.domain.dto.CredentialStatus;
import io.openleap.iam.principal.domain.dto.CreateDevicePrincipalCommand;
import io.openleap.iam.principal.domain.dto.CrossTenantSearchResult;
import io.openleap.iam.principal.domain.dto.HeartbeatUpdated;
import io.openleap.iam.principal.domain.dto.ListTenantMembershipsResult;
import io.openleap.iam.principal.domain.dto.TenantMembershipAdded;
import io.openleap.iam.principal.domain.dto.UpdateHeartbeatCommand;
import io.openleap.iam.principal.domain.dto.CreateHumanPrincipalCommand;
import io.openleap.iam.principal.domain.dto.CreateServicePrincipalCommand;
import io.openleap.iam.principal.domain.dto.CreateSystemPrincipalCommand;
import io.openleap.iam.principal.domain.dto.DeactivatePrincipalCommand;
import io.openleap.iam.principal.domain.dto.CredentialsRotated;
import io.openleap.iam.principal.domain.dto.DeletePrincipalGdprCommand;
import io.openleap.iam.principal.domain.dto.ProfileDetails;
import io.openleap.iam.principal.domain.dto.UpdateCommonAttributesCommand;
import io.openleap.iam.principal.domain.dto.DevicePrincipalCreated;
import io.openleap.iam.principal.domain.dto.HumanPrincipalCreated;
import io.openleap.iam.principal.domain.dto.PrincipalActivated;
import io.openleap.iam.principal.domain.dto.PrincipalDeactivated;
import io.openleap.iam.principal.domain.dto.PrincipalDeleted;
import io.openleap.iam.principal.domain.dto.PrincipalDetails;
import io.openleap.iam.principal.domain.dto.PrincipalSuspended;
import io.openleap.iam.principal.domain.dto.ProfileUpdated;
import io.openleap.iam.principal.domain.dto.RotateCredentialsCommand;
import io.openleap.iam.principal.domain.dto.SearchPrincipalsQuery;
import io.openleap.iam.principal.domain.dto.SearchPrincipalsResult;
import io.openleap.iam.principal.domain.dto.ServicePrincipalCreated;
import io.openleap.iam.principal.domain.dto.SuspendPrincipalCommand;
import io.openleap.iam.principal.domain.dto.SystemPrincipalCreated;
import io.openleap.iam.principal.domain.dto.UpdateProfileCommand;
import io.openleap.iam.principal.domain.entity.HumanPrincipalEntity;
import io.openleap.iam.principal.domain.entity.Principal;
import io.openleap.iam.principal.domain.entity.PrincipalStatus;
import io.openleap.iam.principal.domain.entity.PrincipalType;

import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface PrincipalMapper {

    /**
     * Maps controller request DTO to service domain command.
     */
    @Mapping(target = "username", expression = "java(dto.getUsername() != null ? dto.getUsername().toLowerCase() : null)")
    @Mapping(target = "email", expression = "java(dto.getEmail() != null ? dto.getEmail().toLowerCase() : null)")
    CreateHumanPrincipalCommand toCommand(CreateHumanPrincipalRequestDto dto);

    /**
     * Maps service domain result to controller response DTO.
     */
    CreateHumanPrincipalResponseDto toResponseDto(HumanPrincipalCreated created);
    
    /**
     * Maps controller request DTO to service domain command for service principal.
     */
    CreateServicePrincipalCommand toCommand(CreateServicePrincipalRequestDto dto);
    
    /**
     * Maps service domain result to controller response DTO for service principal.
     * Note: This requires custom implementation due to nested ServicePrincipalInfo object.
     */
    default CreateServicePrincipalResponseDto toResponseDto(ServicePrincipalCreated created) {
        CreateServicePrincipalResponseDto dto = new CreateServicePrincipalResponseDto();
        dto.setId(created.id());
        dto.setPrincipalType("SERVICE");
        dto.setUsername(created.username());
        dto.setStatus("ACTIVE");
        dto.setKeycloakClientId(created.keycloakClientId());
        dto.setKeycloakClientSecret(created.keycloakClientSecret());
        dto.setCreatedAt(Instant.now().toString());
        dto.setWarning("Store these credentials securely. They cannot be retrieved again.");
        
        // Create nested ServicePrincipalInfo
        CreateServicePrincipalResponseDto.ServicePrincipalInfo serviceInfo = 
            new CreateServicePrincipalResponseDto.ServicePrincipalInfo();
        serviceInfo.setServiceName(created.serviceName());
        serviceInfo.setApiKey(created.apiKey());
        serviceInfo.setAllowedScopes(created.allowedScopes());
        serviceInfo.setCredentialRotationDate(created.credentialRotationDate().toString());
        dto.setServicePrincipal(serviceInfo);
        
        return dto;
    }
    
    /**
     * Maps controller request DTO to service domain command for system principal.
     */
    CreateSystemPrincipalCommand toCommand(CreateSystemPrincipalRequestDto dto);
    
    /**
     * Maps service domain result to controller response DTO for system principal.
     * Note: This requires custom implementation due to nested SystemPrincipalInfo object.
     */
    default CreateSystemPrincipalResponseDto toResponseDto(SystemPrincipalCreated created) {
        CreateSystemPrincipalResponseDto dto = new CreateSystemPrincipalResponseDto();
        dto.setId(created.id());
        dto.setPrincipalType("SYSTEM");
        dto.setUsername(created.username());
        dto.setStatus("ACTIVE");
        dto.setCreatedAt(Instant.now().toString());
        
        // Create nested SystemPrincipalInfo
        CreateSystemPrincipalResponseDto.SystemPrincipalInfo systemInfo = 
            new CreateSystemPrincipalResponseDto.SystemPrincipalInfo();
        systemInfo.setSystemIdentifier(created.systemIdentifier());
        systemInfo.setIntegrationType(created.integrationType());
        systemInfo.setCertificateThumbprint(created.certificateThumbprint());
        systemInfo.setAllowedOperations(created.allowedOperations());
        dto.setSystemPrincipal(systemInfo);
        
        return dto;
    }
    
    /**
     * Maps controller request DTO to service domain command for device principal.
     */
    CreateDevicePrincipalCommand toCommand(CreateDevicePrincipalRequestDto dto);
    
    /**
     * Maps service domain result to controller response DTO for device principal.
     * Note: This requires custom implementation due to nested DevicePrincipalInfo object.
     */
    default CreateDevicePrincipalResponseDto toResponseDto(DevicePrincipalCreated created) {
        CreateDevicePrincipalResponseDto dto = new CreateDevicePrincipalResponseDto();
        dto.setId(created.id());
        dto.setPrincipalType("DEVICE");
        dto.setUsername(created.username());
        dto.setStatus("ACTIVE");
        dto.setCreatedAt(Instant.now().toString());
        
        // Create nested DevicePrincipalInfo
        CreateDevicePrincipalResponseDto.DevicePrincipalInfo deviceInfo = 
            new CreateDevicePrincipalResponseDto.DevicePrincipalInfo();
        deviceInfo.setDeviceIdentifier(created.deviceIdentifier());
        deviceInfo.setDeviceType(created.deviceType() != null ? created.deviceType().name() : null);
        deviceInfo.setManufacturer(created.manufacturer());
        deviceInfo.setModel(created.model());
        dto.setDevicePrincipal(deviceInfo);
        
        return dto;
    }
    
    /**
     * Maps controller request DTO to service domain command for profile update.
     * Note: This requires custom implementation due to id parameter.
     */
    default UpdateProfileCommand toCommand(UpdateProfileRequestDto dto, java.util.UUID principalId) {
        return new UpdateProfileCommand(
                principalId,
                dto.getFirstName(),
                dto.getLastName(),
                dto.getDisplayName(),
                dto.getPhone(),
                dto.getLanguage(),
                dto.getTimezone(),
                dto.getLocale(),
                dto.getAvatarUrl(),
                dto.getBio(),
                dto.getPreferences(),
                dto.getContextTags()
        );
    }
    
    /**
     * Maps service domain result and entity to controller response DTO for profile update.
     */
    default UpdateProfileResponseDto toResponseDto(HumanPrincipalEntity principal) {
        UpdateProfileResponseDto dto = new UpdateProfileResponseDto();
        dto.setId(principal.getBusinessId().toString());
        dto.setFirstName(principal.getFirstName());
        dto.setLastName(principal.getLastName());
        dto.setDisplayName(principal.getDisplayName());
        dto.setPhone(principal.getPhone());
        dto.setLanguage(principal.getLanguage());
        dto.setTimezone(principal.getTimezone());
        dto.setLocale(principal.getLocale());
        dto.setAvatarUrl(principal.getAvatarUrl());
        dto.setBio(principal.getBio());
        dto.setPreferences(principal.getPreferences());
        dto.setContextTags(principal.getContextTags());
        dto.setUpdatedAt(principal.getUpdatedAt() != null ? principal.getUpdatedAt().toString() : null);
        return dto;
    }
    
    /**
     * Maps controller request DTO to service domain command for principal activation.
     * Note: This requires custom implementation due to id parameter.
     */
    default ActivatePrincipalCommand toCommand(ActivatePrincipalRequestDto dto, java.util.UUID principalId) {
        return new ActivatePrincipalCommand(
                principalId,
                dto.getVerificationToken(),
                dto.getAdminOverride(),
                dto.getReason()
        );
    }
    
    /**
     * Maps service domain result to controller response DTO for principal activation.
     */
    default ActivatePrincipalResponseDto toResponseDto(PrincipalActivated activated) {
        ActivatePrincipalResponseDto dto = new ActivatePrincipalResponseDto();
        dto.setId(activated.id().toString());
        dto.setStatus("ACTIVE");
        return dto;
    }
    
    /**
     * Maps controller request DTO to service domain command for principal suspension.
     * Note: This requires custom implementation due to id parameter.
     */
    default SuspendPrincipalCommand toCommand(SuspendPrincipalRequestDto dto, java.util.UUID principalId) {
        return new SuspendPrincipalCommand(
                principalId,
                dto.getReason(),
                dto.getIncidentTicket()
        );
    }
    
    /**
     * Maps service domain result to controller response DTO for principal suspension.
     */
    default SuspendPrincipalResponseDto toResponseDto(PrincipalSuspended suspended) {
        SuspendPrincipalResponseDto dto = new SuspendPrincipalResponseDto();
        dto.setId(suspended.id().toString());
        dto.setStatus("SUSPENDED");
        return dto;
    }

    /**
     * Maps controller request DTO to service domain command for principal deactivation.
     * Note: This requires custom implementation due to id parameter.
     */
    default DeactivatePrincipalCommand toCommand(DeactivatePrincipalRequestDto dto, java.util.UUID principalId) {
        return new DeactivatePrincipalCommand(
                principalId,
                dto.getReason(),
                dto.getEffectiveDate()
        );
    }

    /**
     * Maps service domain result to controller response DTO for principal deactivation.
     */
    default DeactivatePrincipalResponseDto toResponseDto(PrincipalDeactivated deactivated) {
        DeactivatePrincipalResponseDto dto = new DeactivatePrincipalResponseDto();
        dto.setId(deactivated.id().toString());
        dto.setStatus("INACTIVE");
        return dto;
    }

    /**
     * Maps controller request DTO to service domain command for GDPR deletion.
     * Note: This requires custom implementation due to id parameter.
     */
    default DeletePrincipalGdprCommand toCommand(DeletePrincipalGdprRequestDto dto, java.util.UUID principalId) {
        return new DeletePrincipalGdprCommand(
                principalId,
                dto.getConfirmation(),
                dto.getGdprRequestTicket(),
                dto.getRequestorEmail()
        );
    }

    /**
     * Maps service domain result to controller response DTO for GDPR deletion.
     */
    default DeletePrincipalGdprResponseDto toResponseDto(PrincipalDeleted deleted) {
        DeletePrincipalGdprResponseDto dto = new DeletePrincipalGdprResponseDto();
        dto.setId(deleted.id().toString());
        dto.setStatus("DELETED");
        dto.setAnonymized(deleted.anonymized());
        dto.setAuditReference(deleted.auditReference());
        dto.setDeletedAt(deleted.deletedAt().toString());
        return dto;
    }

    /**
     * Maps controller request DTO to service domain command for credentials rotation.
     * Note: This requires custom implementation due to id parameter.
     */
    default RotateCredentialsCommand toCommand(RotateCredentialsRequestDto dto, java.util.UUID principalId) {
        return new RotateCredentialsCommand(
                principalId,
                dto.getForce(),
                dto.getReason()
        );
    }

    /**
     * Maps service domain result to controller response DTO for credentials rotation.
     */
    default RotateCredentialsResponseDto toResponseDto(CredentialsRotated rotated) {
        RotateCredentialsResponseDto dto = new RotateCredentialsResponseDto();
        dto.setId(rotated.id().toString());
        dto.setApiKey(rotated.apiKey());
        dto.setKeycloakClientSecret(rotated.keycloakClientSecret());
        dto.setCredentialRotationDate(rotated.credentialRotationDate().toString());
        dto.setRotatedAt(rotated.rotatedAt().toString());
        dto.setWarning("Store these credentials securely. They cannot be retrieved again.");
        return dto;
    }

    /**
     * Maps service domain result to controller response DTO for get principal.
     */
    default GetPrincipalResponseDto toResponseDto(PrincipalDetails details) {
        GetPrincipalResponseDto dto = new GetPrincipalResponseDto();
        dto.setId(details.id().toString());
        dto.setPrincipalType(details.principalType());
        dto.setUsername(details.username());
        dto.setEmail(details.email());
        dto.setStatus(details.status());
//        dto.setPrimaryTenantId(details.defaultTenantId() != null ? details.defaultTenantId().toString() : null);
        dto.setCreatedAt(details.createdAt() != null ? details.createdAt().toString() : null);
        dto.setUpdatedAt(details.updatedAt() != null ? details.updatedAt().toString() : null);
        dto.setContextTags(details.contextTags());

        // Human principal specific fields
        dto.setEmailVerified(details.emailVerified());
        dto.setMfaEnabled(details.mfaEnabled());
        dto.setLastLoginAt(details.lastLoginAt() != null ? details.lastLoginAt().toString() : null);
        dto.setDisplayName(details.displayName());
        dto.setFirstName(details.firstName());
        dto.setLastName(details.lastName());

        // Service principal specific fields
        dto.setServiceName(details.serviceName());
        dto.setAllowedScopes(details.allowedScopes());
        dto.setCredentialRotationDate(details.credentialRotationDate() != null ? details.credentialRotationDate().toString() : null);

        // System principal specific fields
        dto.setSystemIdentifier(details.systemIdentifier());
        dto.setIntegrationType(details.integrationType());
        dto.setAllowedOperations(details.allowedOperations());

        // Device principal specific fields
        dto.setDeviceIdentifier(details.deviceIdentifier());
        dto.setDeviceType(details.deviceType());
        dto.setManufacturer(details.manufacturer());
        dto.setModel(details.model());

        return dto;
    }

    /**
     * Maps controller request DTO to service domain command for common attributes update.
     * Note: This requires custom implementation due to id parameter.
     */
    default UpdateCommonAttributesCommand toCommand(UpdateCommonAttributesRequestDto dto, java.util.UUID principalId) {
        return new UpdateCommonAttributesCommand(
                principalId,
                dto.getContextTags()
        );
    }

    /**
     * Maps service domain result and entity to controller response DTO for common attributes update.
     */
    default UpdateCommonAttributesResponseDto toResponseDto(CommonAttributesUpdated updated, Principal principal) {
        UpdateCommonAttributesResponseDto dto = new UpdateCommonAttributesResponseDto();
        dto.setId(principal.getBusinessId().toString());
        dto.setContextTags(principal.getContextTags());
        dto.setUpdatedAt(principal.getUpdatedAt() != null ? principal.getUpdatedAt().toString() : null);
        return dto;
    }

    /**
     * Maps service domain result to controller response DTO for get profile.
     */
    default GetProfileResponseDto toResponseDto(ProfileDetails details) {
        GetProfileResponseDto dto = new GetProfileResponseDto();
        dto.setId(details.id().toString());
        dto.setFirstName(details.firstName());
        dto.setLastName(details.lastName());
        dto.setDisplayName(details.displayName());
        dto.setPhone(details.phone());
        dto.setLanguage(details.language());
        dto.setTimezone(details.timezone());
        dto.setLocale(details.locale());
        dto.setAvatarUrl(details.avatarUrl());
        dto.setBio(details.bio());
        dto.setPreferences(details.preferences());
        return dto;
    }

    /**
     * Maps service domain result to controller response DTO for credential status.
     */
    default GetCredentialStatusResponseDto toResponseDto(CredentialStatus status) {
        GetCredentialStatusResponseDto dto = new GetCredentialStatusResponseDto();
        dto.setId(status.id().toString());
        dto.setPrincipalType(status.principalType());
        dto.setCredentialRotationDate(status.credentialRotationDate() != null ? status.credentialRotationDate().toString() : null);
        dto.setDaysUntilRotation(status.daysUntilRotation());
        dto.setLastRotatedAt(status.lastRotatedAt() != null ? status.lastRotatedAt().toString() : null);
        dto.setRotationRequired(status.rotationRequired());
        dto.setHasApiKey(status.hasApiKey());
        dto.setHasCertificate(status.hasCertificate());
        return dto;
    }

    /**
     * Maps service domain result to controller response DTO for list tenant memberships.
     */
    default ListTenantMembershipsResponseDto toResponseDto(ListTenantMembershipsResult result) {
        ListTenantMembershipsResponseDto dto = new ListTenantMembershipsResponseDto();
        dto.setTotal(result.total());
        dto.setPage(result.page());
        dto.setSize(result.size());

        var items = result.items().stream()
                .map(item -> {
                    var itemDto = new ListTenantMembershipsResponseDto.TenantMembershipItemDto();
                    itemDto.setId(item.id().toString());
                    itemDto.setPrincipalId(item.principalId().toString());
                    itemDto.setValidFrom(item.validFrom() != null ? item.validFrom().toString() : null);
                    itemDto.setValidTo(item.validTo() != null ? item.validTo().toString() : null);
                    itemDto.setStatus(item.status());
                    return itemDto;
                })
                .toList();
        dto.setItems(items);

        return dto;
    }

    /**
     * Maps request DTO to command for adding tenant membership.
     */
    default AddTenantMembershipCommand toCommand(AddTenantMembershipRequestDto dto, java.util.UUID principalId, java.util.UUID invitedBy) {
        java.time.LocalDate validFrom = null;
        java.time.LocalDate validTo = null;
        if (dto.getValidFrom() != null && !dto.getValidFrom().isBlank()) {
            validFrom = java.time.LocalDate.parse(dto.getValidFrom());
        }
        if (dto.getValidTo() != null && !dto.getValidTo().isBlank()) {
            validTo = java.time.LocalDate.parse(dto.getValidTo());
        }
        return new AddTenantMembershipCommand(
                principalId,
                java.util.UUID.fromString(dto.getTenantId()),
                validFrom,
                validTo,
                invitedBy
        );
    }

    /**
     * Maps result to response DTO for adding tenant membership.
     */
    default AddTenantMembershipResponseDto toResponseDto(TenantMembershipAdded added) {
        AddTenantMembershipResponseDto dto = new AddTenantMembershipResponseDto();
        dto.setId(added.id().toString());
        dto.setPrincipalId(added.principalId().toString());
        dto.setTenantId(added.tenantId().toString());
        dto.setValidFrom(added.validFrom() != null ? added.validFrom().toString() : null);
        dto.setValidTo(added.validTo() != null ? added.validTo().toString() : null);
        dto.setStatus(added.status());
        dto.setInvitedBy(added.invitedBy() != null ? added.invitedBy().toString() : null);
        dto.setCreatedAt(added.createdAt() != null ? added.createdAt().toString() : null);
        return dto;
    }

    /**
     * Maps request DTO to command for updating heartbeat.
     */
    default UpdateHeartbeatCommand toCommand(UpdateHeartbeatRequestDto dto, java.util.UUID principalId) {
        return new UpdateHeartbeatCommand(
                principalId,
                dto.getFirmwareVersion(),
                dto.getLocationInfo()
        );
    }

    /**
     * Maps result to response DTO for updating heartbeat.
     */
    default UpdateHeartbeatResponseDto toResponseDto(HeartbeatUpdated updated) {
        UpdateHeartbeatResponseDto dto = new UpdateHeartbeatResponseDto();
        dto.setId(updated.id().toString());
        dto.setLastHeartbeatAt(updated.lastHeartbeatAt() != null ? updated.lastHeartbeatAt().toString() : null);
        return dto;
    }

    /**
     * Maps result to response DTO for cross-tenant search.
     */
    default CrossTenantSearchResponseDto toResponseDto(CrossTenantSearchResult result) {
        CrossTenantSearchResponseDto dto = new CrossTenantSearchResponseDto();
        dto.setTotal(result.total());
        dto.setPage(result.page());
        dto.setSize(result.size());

        var items = result.items().stream()
                .map(item -> {
                    var itemDto = new CrossTenantSearchResponseDto.CrossTenantPrincipalItemDto();
                    itemDto.setPrincipalId(item.id().toString());
                    itemDto.setPrincipalType(item.principalType());
                    itemDto.setUsername(item.username());
                    itemDto.setEmail(item.email());
                    itemDto.setStatus(item.status());
                    itemDto.setPrimaryTenantId(item.defaultTenantId() != null ? item.defaultTenantId().toString() : null);
                    return itemDto;
                })
                .toList();
        dto.setItems(items);

        return dto;
    }

    /**
     * Maps request parameters to search principals query.
     */
    default SearchPrincipalsQuery toQuery(String search, String principalType, String status, UUID tenantId, int page, int size) {
        PrincipalType parsedPrincipalType = null;
        if (principalType != null && !principalType.isBlank()) {
            try {
                parsedPrincipalType = PrincipalType.valueOf(principalType.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid principal type, leave as null
            }
        }

        PrincipalStatus parsedStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                parsedStatus = PrincipalStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, leave as null
            }
        }

        return new SearchPrincipalsQuery(search, parsedPrincipalType, parsedStatus, tenantId, page, size);
    }

    /**
     * Maps search principals result to response DTO.
     */
    default SearchPrincipalsResponseDto toResponseDto(SearchPrincipalsResult result) {
        SearchPrincipalsResponseDto dto = new SearchPrincipalsResponseDto();
        dto.setTotal(result.total());
        dto.setPage(result.page());
        dto.setSize(result.size());

        var items = result.items().stream()
                .map(item -> {
                    var itemDto = new SearchPrincipalsResponseDto.PrincipalSearchItem();
                    itemDto.setId(item.principalId().toString());
                    itemDto.setUsername(item.username());
                    itemDto.setEmail(item.email());
                    itemDto.setPrincipalType(item.principalType());
                    itemDto.setStatus(item.status());
                    itemDto.setDefaultTenantId(item.primaryTenantId() != null ? item.primaryTenantId().toString() : null);
                    itemDto.setLastLoginAt(item.lastLoginAt() != null ? item.lastLoginAt().toString() : null);
                    itemDto.setCreatedAt(item.createdAt() != null ? item.createdAt().toString() : null);
                    return itemDto;
                })
                .toList();
        dto.setItems(items);

        return dto;
    }
}

