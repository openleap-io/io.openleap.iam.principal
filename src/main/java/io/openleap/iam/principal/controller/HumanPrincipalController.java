package io.openleap.iam.principal.controller;

import io.openleap.iam.principal.controller.dto.ActivatePrincipalRequestDto;
import io.openleap.iam.principal.controller.dto.ActivatePrincipalResponseDto;
import io.openleap.iam.principal.controller.dto.CreateHumanPrincipalRequestDto;
import io.openleap.iam.principal.controller.dto.CreateHumanPrincipalResponseDto;
import io.openleap.iam.principal.controller.dto.SuspendPrincipalRequestDto;
import io.openleap.iam.principal.controller.dto.SuspendPrincipalResponseDto;
import io.openleap.iam.principal.controller.dto.UpdateProfileRequestDto;
import io.openleap.iam.principal.controller.dto.UpdateProfileResponseDto;
import io.openleap.iam.principal.controller.mapper.PrincipalMapper;
import io.openleap.iam.principal.repository.HumanPrincipalRepository;
import io.openleap.iam.principal.service.HumanPrincipalService;
import io.openleap.iam.principal.service.PrincipalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/iam/principals")
public class HumanPrincipalController {
    
    private final HumanPrincipalService humanPrincipalService;
    private final PrincipalMapper principalMapper;
    private final HumanPrincipalRepository humanPrincipalRepository;
    private final PrincipalService principalService;

    public HumanPrincipalController(
            HumanPrincipalService humanPrincipalService,
            PrincipalMapper principalMapper,
            HumanPrincipalRepository humanPrincipalRepository,
            PrincipalService principalService) {
        this.humanPrincipalService = humanPrincipalService;
        this.principalMapper = principalMapper;
        this.humanPrincipalRepository = humanPrincipalRepository;
        this.principalService = principalService;
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
    
    /**
     * Update a human principal profile.
     * 
     * Requires permission: Self-update (principal_id matches authenticated principal) 
     *                     OR iam.principal.profile:update (admin)
     * 
     * @param principalId the principal ID
     * @param request the update request DTO
     * @return response DTO containing the updated profile
     */
    @PatchMapping("/{principalId}/profile")
    public ResponseEntity<UpdateProfileResponseDto> updateProfile(
            @PathVariable UUID principalId,
            @Valid @RequestBody UpdateProfileRequestDto request) {
        var command = principalMapper.toCommand(request, principalId);
        var updated = humanPrincipalService.updateProfile(command);
        var principal = humanPrincipalRepository.findById(principalId)
                .orElseThrow(() -> new RuntimeException("Principal not found: " + principalId));
        var response = principalMapper.toResponseDto(updated, principal);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Activate a principal.
     * 
     * Requires permission: iam.principal:activate
     * 
     * @param principalId the principal ID
     * @param request the activation request DTO
     * @return response DTO containing the principal_id and status
     */
    @PostMapping("/{principalId}/activate")
    public ResponseEntity<ActivatePrincipalResponseDto> activatePrincipal(
            @PathVariable UUID principalId,
            @Valid @RequestBody ActivatePrincipalRequestDto request) {
        var command = principalMapper.toCommand(request, principalId);
        var activated = principalService.activatePrincipal(command);
        var response = principalMapper.toResponseDto(activated);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Suspend a principal.
     * 
     * Requires permission: iam.principal:suspend
     * 
     * @param principalId the principal ID
     * @param request the suspension request DTO
     * @return response DTO containing the principal_id and status
     */
    @PostMapping("/{principalId}/suspend")
    public ResponseEntity<SuspendPrincipalResponseDto> suspendPrincipal(
            @PathVariable UUID principalId,
            @Valid @RequestBody SuspendPrincipalRequestDto request) {
        var command = principalMapper.toCommand(request, principalId);
        var suspended = principalService.suspendPrincipal(command);
        var response = principalMapper.toResponseDto(suspended);
        return ResponseEntity.ok(response);
    }
}
