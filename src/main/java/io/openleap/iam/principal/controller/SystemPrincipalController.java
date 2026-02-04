package io.openleap.iam.principal.controller;

import io.openleap.iam.principal.controller.dto.CreateSystemPrincipalRequestDto;
import io.openleap.iam.principal.controller.dto.CreateSystemPrincipalResponseDto;
import io.openleap.iam.principal.controller.mapper.PrincipalMapper;
import io.openleap.iam.principal.service.SystemPrincipalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/iam/principals")
public class SystemPrincipalController {
    
    private final SystemPrincipalService systemPrincipalService;
    private final PrincipalMapper principalMapper;

    public SystemPrincipalController(
            SystemPrincipalService systemPrincipalService,
            PrincipalMapper principalMapper) {
        this.systemPrincipalService = systemPrincipalService;
        this.principalMapper = principalMapper;
    }

    @PostMapping("/system")
    public ResponseEntity<CreateSystemPrincipalResponseDto> createSystemPrincipal(
            @Valid @RequestBody CreateSystemPrincipalRequestDto request) {
        var command = principalMapper.toCommand(request);
        var created = systemPrincipalService.createSystemPrincipal(command);
        var response = principalMapper.toResponseDto(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
