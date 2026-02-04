package io.openleap.iam.principal.controller;

import io.openleap.iam.principal.controller.dto.*;
import io.openleap.iam.principal.controller.mapper.PrincipalMapper;
import io.openleap.iam.principal.domain.dto.ProfileUpdated;
import io.openleap.iam.principal.domain.entity.PrincipalId;
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

    @PostMapping
    public ResponseEntity<CreateHumanPrincipalResponseDto> createHumanPrincipal(
            @Valid @RequestBody CreateHumanPrincipalRequestDto request) {
        var command = principalMapper.toCommand(request);
        var created = humanPrincipalService.createHumanPrincipal(command);
        var response = principalMapper.toResponseDto(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{principalId}/profile")
    public ResponseEntity<ProfileUpdated> updateProfile(
            @PathVariable UUID principalId,
            @Valid @RequestBody UpdateProfileRequestDto request) {
        var command = principalMapper.toCommand(request, principalId);
        var updated = humanPrincipalService.updateProfile(command);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{principalId}/profile")
    public ResponseEntity<GetProfileResponseDto> getProfile(@PathVariable UUID principalId) {
        var details = humanPrincipalService.getProfile(principalId);
        var response = principalMapper.toResponseDto(details);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{principalId}/credentials/status")
    public ResponseEntity<GetCredentialStatusResponseDto> getCredentialStatus(@PathVariable UUID principalId) {
        var status = principalService.getCredentialStatus(PrincipalId.of(principalId));
        var response = principalMapper.toResponseDto(status);
        return ResponseEntity.ok(response);
    }


    // TODO: to be moved to iam.tenant
//    @GetMapping("/{id}/tenants")
//    public ResponseEntity<ListTenantMembershipsResponseDto> listTenantMemberships(
//            @PathVariable UUID id,
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "50") int size) {
//        var result = principalService.listTenantMemberships(id, page, size);
//        var response = principalMapper.toResponseDto(result);
//        return ResponseEntity.ok(response);
//    }


    // TODO: to be moved to iam.tenant
//    @PostMapping("/{id}/tenants")
//    public ResponseEntity<AddTenantMembershipResponseDto> addTenantMembership(
//            @PathVariable UUID id,
//            @Valid @RequestBody AddTenantMembershipRequestDto request) {
//        // For now, use null as invitedBy - in production this would come from the authenticated user
//        var command = principalMapper.toCommand(request, id, null);
//        var added = principalService.addTenantMembership(command);
//        var response = principalMapper.toResponseDto(added);
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }


    // TODO: to be moved to iam.tenant
//    @DeleteMapping("/{id}/tenants/{tenantId}")
//    public ResponseEntity<Void> removeTenantMembership(
//            @PathVariable UUID id,
//            @PathVariable UUID tenantId) {
//        var command = new RemoveTenantMembershipCommand(id, tenantId);
//        principalService.removeTenantMembership(command);
//        return ResponseEntity.noContent().build();
//    }


    @PostMapping("/{principalId}/activate")
    public ResponseEntity<ActivatePrincipalResponseDto> activatePrincipal(
            @PathVariable UUID principalId,
            @Valid @RequestBody ActivatePrincipalRequestDto request) {
        var command = principalMapper.toCommand(request, principalId);
        var activated = principalService.activatePrincipal(command);
        var response = principalMapper.toResponseDto(activated);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/{principalId}/suspend")
    public ResponseEntity<SuspendPrincipalResponseDto> suspendPrincipal(
            @PathVariable UUID principalId,
            @Valid @RequestBody SuspendPrincipalRequestDto request) {
        var command = principalMapper.toCommand(request, principalId);
        var suspended = principalService.suspendPrincipal(command);
        var response = principalMapper.toResponseDto(suspended);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{principalId}/deactivate")
    public ResponseEntity<DeactivatePrincipalResponseDto> deactivatePrincipal(
            @PathVariable UUID principalId,
            @Valid @RequestBody DeactivatePrincipalRequestDto request) {
        var command = principalMapper.toCommand(request, principalId);
        var deactivated = principalService.deactivatePrincipal(command);
        var response = principalMapper.toResponseDto(deactivated);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{principalId}/gdpr")
    public ResponseEntity<DeletePrincipalGdprResponseDto> deletePrincipalGdpr(
            @PathVariable UUID principalId,
            @Valid @RequestBody DeletePrincipalGdprRequestDto request) {
        var command = principalMapper.toCommand(request, principalId);
        var deleted = principalService.deletePrincipalGdpr(command);
        var response = principalMapper.toResponseDto(deleted);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{principalId}")
    public ResponseEntity<GetPrincipalResponseDto> getPrincipal(@PathVariable UUID principalId) {
        var details = principalService.getPrincipalDetails(PrincipalId.of(principalId));
        var response = principalMapper.toResponseDto(details);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{principalId}")
    public ResponseEntity<UpdateCommonAttributesResponseDto> updateCommonAttributes(
            @PathVariable UUID principalId,
            @Valid @RequestBody UpdateCommonAttributesRequestDto request) {
        var command = principalMapper.toCommand(request, principalId);
        var updated = principalService.updateCommonAttributes(command);
        var principal = principalService.findPrincipalByBusinessId(PrincipalId.of(principalId))
                .orElseThrow(() -> new RuntimeException("Principal not found: " + principalId));
        var response = principalMapper.toResponseDto(updated, principal);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<SearchPrincipalsResponseDto> searchPrincipals(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, name = "principal_type") String principalType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, name = "tenant_id") UUID tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        var query = principalMapper.toQuery(search, principalType, status, tenantId, page, size);
        var result = principalService.searchPrincipals(query);
        var response = principalMapper.toResponseDto(result);
        return ResponseEntity.ok(response);
    }
}
