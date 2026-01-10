package io.openleap.iam.principal.controller.mapper;

import io.openleap.iam.principal.controller.dto.CreateHumanPrincipalRequestDto;
import io.openleap.iam.principal.controller.dto.CreateHumanPrincipalResponseDto;
import io.openleap.iam.principal.controller.dto.CreateServicePrincipalRequestDto;
import io.openleap.iam.principal.controller.dto.CreateServicePrincipalResponseDto;
import io.openleap.iam.principal.controller.dto.CreateSystemPrincipalRequestDto;
import io.openleap.iam.principal.controller.dto.CreateSystemPrincipalResponseDto;
import io.openleap.iam.principal.domain.dto.CreateHumanPrincipalCommand;
import io.openleap.iam.principal.domain.dto.CreateServicePrincipalCommand;
import io.openleap.iam.principal.domain.dto.CreateSystemPrincipalCommand;
import io.openleap.iam.principal.domain.dto.HumanPrincipalCreated;
import io.openleap.iam.principal.domain.dto.ServicePrincipalCreated;
import io.openleap.iam.principal.domain.dto.SystemPrincipalCreated;
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
}

