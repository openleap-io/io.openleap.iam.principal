package io.openleap.iam.principal.controller;

import io.openleap.iam.principal.controller.dto.CreateServicePrincipalRequestDto;
import io.openleap.iam.principal.controller.dto.CreateServicePrincipalResponseDto;
import io.openleap.iam.principal.controller.mapper.PrincipalMapper;
import io.openleap.iam.principal.service.ServicePrincipalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/iam/principals")
public class ServicePrincipalController {
    
    private final ServicePrincipalService servicePrincipalService;
    private final PrincipalMapper principalMapper;

    public ServicePrincipalController(
            ServicePrincipalService servicePrincipalService,
            PrincipalMapper principalMapper) {
        this.servicePrincipalService = servicePrincipalService;
        this.principalMapper = principalMapper;
    }
    
    /**
     * Create a new service principal.
     * 
     * Requires permission: iam.service_principal:create
     * 
     * @param request the create request DTO
     * @return response DTO containing the principal_id, API key, and Keycloak client secret
     */
    @PostMapping("/service")
    public ResponseEntity<CreateServicePrincipalResponseDto> createServicePrincipal(
            @Valid @RequestBody CreateServicePrincipalRequestDto request) {
        var command = principalMapper.toCommand(request);
        var created = servicePrincipalService.createServicePrincipal(command);
        var response = principalMapper.toResponseDto(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
