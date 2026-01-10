package io.openleap.iam.principal.controller;

import io.openleap.iam.principal.controller.dto.CreateHumanPrincipalRequestDto;
import io.openleap.iam.principal.controller.dto.CreateHumanPrincipalResponseDto;
import io.openleap.iam.principal.controller.mapper.PrincipalMapper;
import io.openleap.iam.principal.service.HumanPrincipalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/iam/principals")
public class HumanPrincipalController {
    
    private final HumanPrincipalService humanPrincipalService;
    private final PrincipalMapper principalMapper;

    public HumanPrincipalController(
            HumanPrincipalService humanPrincipalService,
            PrincipalMapper principalMapper) {
        this.humanPrincipalService = humanPrincipalService;
        this.principalMapper = principalMapper;
    }
    
    /**
     * Create a new human principal.
     * 
     * Requires permission: iam.principal:create
     * 
     * @param request the create request DTO
     * @return response DTO containing the principal_id
     */
    @PostMapping
    public ResponseEntity<CreateHumanPrincipalResponseDto> createHumanPrincipal(
            @Valid @RequestBody CreateHumanPrincipalRequestDto request) {
        var command = principalMapper.toCommand(request);
        var created = humanPrincipalService.createHumanPrincipal(command);
        var response = principalMapper.toResponseDto(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
