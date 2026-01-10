package io.openleap.iam.principal.mapper;

import io.openleap.iam.principal.controller.dto.CreateHumanPrincipalRequestDto;
import io.openleap.iam.principal.controller.dto.CreateHumanPrincipalResponseDto;
import io.openleap.iam.principal.domain.dto.CreateHumanPrincipalCommand;
import io.openleap.iam.principal.domain.dto.HumanPrincipalCreated;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

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
}

