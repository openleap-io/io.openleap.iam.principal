package io.openleap.iam.principal.controller.mapper;

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
import io.openleap.iam.principal.domain.dto.CreateDevicePrincipalCommand;
import io.openleap.iam.principal.domain.dto.CreateHumanPrincipalCommand;
import io.openleap.iam.principal.domain.dto.CreateServicePrincipalCommand;
import io.openleap.iam.principal.domain.dto.CreateSystemPrincipalCommand;
import io.openleap.iam.principal.domain.dto.DevicePrincipalCreated;
import io.openleap.iam.principal.domain.dto.HumanPrincipalCreated;
import io.openleap.iam.principal.domain.dto.ProfileUpdated;
import io.openleap.iam.principal.domain.dto.ServicePrincipalCreated;
import io.openleap.iam.principal.domain.dto.SystemPrincipalCreated;
import io.openleap.iam.principal.domain.dto.UpdateProfileCommand;
import io.openleap.iam.principal.domain.entity.HumanPrincipalEntity;
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
        dto.setPrincipalId(created.principalId());
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
        dto.setPrincipalId(created.principalId());
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
        dto.setPrincipalId(created.principalId());
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
     * Note: This requires custom implementation due to principalId parameter.
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
    default UpdateProfileResponseDto toResponseDto(ProfileUpdated updated, HumanPrincipalEntity principal) {
        UpdateProfileResponseDto dto = new UpdateProfileResponseDto();
        dto.setPrincipalId(principal.getPrincipalId().toString());
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
}

