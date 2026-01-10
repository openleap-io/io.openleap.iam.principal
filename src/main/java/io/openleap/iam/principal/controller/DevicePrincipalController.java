package io.openleap.iam.principal.controller;

import io.openleap.iam.principal.controller.dto.CreateDevicePrincipalRequestDto;
import io.openleap.iam.principal.controller.dto.CreateDevicePrincipalResponseDto;
import io.openleap.iam.principal.controller.mapper.PrincipalMapper;
import io.openleap.iam.principal.service.DevicePrincipalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/iam/principals")
public class DevicePrincipalController {
    
    private final DevicePrincipalService devicePrincipalService;
    private final PrincipalMapper principalMapper;

    public DevicePrincipalController(
            DevicePrincipalService devicePrincipalService,
            PrincipalMapper principalMapper) {
        this.devicePrincipalService = devicePrincipalService;
        this.principalMapper = principalMapper;
    }
    
    /**
     * Create a new device principal.
     * 
     * Requires permission: iam.device_principal:create
     * 
     * @param request the create request DTO
     * @return response DTO containing the principal_id
     */
    @PostMapping("/device")
    public ResponseEntity<CreateDevicePrincipalResponseDto> createDevicePrincipal(
            @Valid @RequestBody CreateDevicePrincipalRequestDto request) {
        var command = principalMapper.toCommand(request);
        var created = devicePrincipalService.createDevicePrincipal(command);
        var response = principalMapper.toResponseDto(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
