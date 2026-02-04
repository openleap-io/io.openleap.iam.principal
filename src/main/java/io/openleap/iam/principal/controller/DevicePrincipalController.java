package io.openleap.iam.principal.controller;

import io.openleap.iam.principal.controller.dto.CreateDevicePrincipalRequestDto;
import io.openleap.iam.principal.controller.dto.CreateDevicePrincipalResponseDto;
import io.openleap.iam.principal.controller.dto.UpdateHeartbeatRequestDto;
import io.openleap.iam.principal.controller.dto.UpdateHeartbeatResponseDto;
import io.openleap.iam.principal.controller.mapper.PrincipalMapper;
import io.openleap.iam.principal.service.DevicePrincipalService;
import io.openleap.iam.principal.service.PrincipalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/iam/principals")
public class DevicePrincipalController {
    
    private final DevicePrincipalService devicePrincipalService;
    private final PrincipalService principalService;
    private final PrincipalMapper principalMapper;

    public DevicePrincipalController(DevicePrincipalService devicePrincipalService, PrincipalService principalService, PrincipalMapper principalMapper) {
        this.devicePrincipalService = devicePrincipalService;
        this.principalService = principalService;
        this.principalMapper = principalMapper;
    }

    @PostMapping("/device")
    public ResponseEntity<CreateDevicePrincipalResponseDto> createDevicePrincipal(
            @Valid @RequestBody CreateDevicePrincipalRequestDto request) {
        var command = principalMapper.toCommand(request);
        var created = devicePrincipalService.createDevicePrincipal(command);
        var response = principalMapper.toResponseDto(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{principalId}/heartbeat")
    public ResponseEntity<UpdateHeartbeatResponseDto> updateHeartbeat(
            @PathVariable UUID principalId,
            @Valid @RequestBody UpdateHeartbeatRequestDto request) {
        var command = principalMapper.toCommand(request, principalId);
        var updated = principalService.updateHeartbeat(command);
        var response = principalMapper.toResponseDto(updated);
        return ResponseEntity.ok(response);
    }
}
