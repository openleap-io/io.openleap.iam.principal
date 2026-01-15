package io.openleap.iam.principal.controller;

import io.openleap.iam.principal.controller.dto.ActivatePrincipalRequestDto;
import io.openleap.iam.principal.controller.dto.ActivatePrincipalResponseDto;
import io.openleap.iam.principal.controller.dto.AddTenantMembershipRequestDto;
import io.openleap.iam.principal.controller.dto.AddTenantMembershipResponseDto;
import io.openleap.iam.principal.controller.dto.CreateHumanPrincipalRequestDto;
import io.openleap.iam.principal.controller.dto.CreateHumanPrincipalResponseDto;
import io.openleap.iam.principal.controller.dto.DeactivatePrincipalRequestDto;
import io.openleap.iam.principal.controller.dto.DeactivatePrincipalResponseDto;
import io.openleap.iam.principal.controller.dto.DeletePrincipalGdprRequestDto;
import io.openleap.iam.principal.controller.dto.DeletePrincipalGdprResponseDto;
import io.openleap.iam.principal.controller.dto.GetCredentialStatusResponseDto;
import io.openleap.iam.principal.controller.dto.GetPrincipalResponseDto;
import io.openleap.iam.principal.controller.dto.GetProfileResponseDto;
import io.openleap.iam.principal.controller.dto.ListTenantMembershipsResponseDto;
import io.openleap.iam.principal.controller.dto.SearchPrincipalsResponseDto;
import io.openleap.iam.principal.controller.dto.SuspendPrincipalRequestDto;
import io.openleap.iam.principal.controller.dto.SuspendPrincipalResponseDto;
import io.openleap.iam.principal.controller.dto.UpdateCommonAttributesRequestDto;
import io.openleap.iam.principal.controller.dto.UpdateCommonAttributesResponseDto;
import io.openleap.iam.principal.controller.dto.UpdateHeartbeatRequestDto;
import io.openleap.iam.principal.controller.dto.UpdateHeartbeatResponseDto;
import io.openleap.iam.principal.controller.dto.UpdateProfileRequestDto;
import io.openleap.iam.principal.controller.dto.UpdateProfileResponseDto;
import io.openleap.iam.principal.domain.dto.RemoveTenantMembershipCommand;
import io.openleap.iam.principal.domain.dto.SearchPrincipalsQuery;
import io.openleap.iam.principal.domain.entity.PrincipalStatus;
import io.openleap.iam.principal.domain.entity.PrincipalType;
import io.openleap.iam.principal.controller.mapper.PrincipalMapper;
import io.openleap.iam.principal.repository.HumanPrincipalRepository;
import io.openleap.iam.principal.service.HumanPrincipalService;
import io.openleap.iam.principal.service.PrincipalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
     * Get a human principal's profile.
     *
     * Requires permission: Self-read (principal_id matches authenticated principal)
     *                     OR iam.principal.profile:read (admin)
     *
     * @param principalId the principal ID
     * @return response DTO containing the profile details
     */
    @GetMapping("/{principalId}/profile")
    public ResponseEntity<GetProfileResponseDto> getProfile(@PathVariable UUID principalId) {
        var details = humanPrincipalService.getProfile(principalId);
        var response = principalMapper.toResponseDto(details);
        return ResponseEntity.ok(response);
    }

    /**
     * Get credential status for a service or system principal.
     *
     * Requires permission: iam.principal.credentials:read
     *
     * @param principalId the principal ID
     * @return response DTO containing the credential status
     */
    @GetMapping("/{principalId}/credentials/status")
    public ResponseEntity<GetCredentialStatusResponseDto> getCredentialStatus(@PathVariable UUID principalId) {
        var status = principalService.getCredentialStatus(principalId);
        var response = principalMapper.toResponseDto(status);
        return ResponseEntity.ok(response);
    }

    /**
     * List tenant memberships for a principal.
     *
     * Requires permission: iam.principal.tenants:read
     *
     * @param principalId the principal ID
     * @param page page number (1-indexed, default 1)
     * @param size page size (default 50, max 100)
     * @return response DTO containing the paginated list of memberships
     */
    @GetMapping("/{principalId}/tenants")
    public ResponseEntity<ListTenantMembershipsResponseDto> listTenantMemberships(
            @PathVariable UUID principalId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        var result = principalService.listTenantMemberships(principalId, page, size);
        var response = principalMapper.toResponseDto(result);
        return ResponseEntity.ok(response);
    }

    /**
     * Add a tenant membership for a principal.
     *
     * Requires permission: iam.principal.tenant:assign
     *
     * @param principalId the principal ID
     * @param request the add tenant membership request DTO
     * @return response DTO containing the membership details
     */
    @PostMapping("/{principalId}/tenants")
    public ResponseEntity<AddTenantMembershipResponseDto> addTenantMembership(
            @PathVariable UUID principalId,
            @Valid @RequestBody AddTenantMembershipRequestDto request) {
        // For now, use null as invitedBy - in production this would come from the authenticated user
        var command = principalMapper.toCommand(request, principalId, null);
        var added = principalService.addTenantMembership(command);
        var response = principalMapper.toResponseDto(added);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Remove a tenant membership for a principal.
     *
     * Requires permission: iam.principal.tenant:remove
     *
     * @param principalId the principal ID
     * @param tenantId the tenant ID
     * @return no content on success
     */
    @DeleteMapping("/{principalId}/tenants/{tenantId}")
    public ResponseEntity<Void> removeTenantMembership(
            @PathVariable UUID principalId,
            @PathVariable UUID tenantId) {
        var command = new RemoveTenantMembershipCommand(principalId, tenantId);
        principalService.removeTenantMembership(command);
        return ResponseEntity.noContent().build();
    }

    /**
     * Update heartbeat for a device principal.
     *
     * Requires permission: iam.device_principal:heartbeat
     *
     * @param principalId the principal ID
     * @param request the heartbeat update request DTO
     * @return response DTO containing the heartbeat timestamp
     */
    @PostMapping("/{principalId}/heartbeat")
    public ResponseEntity<UpdateHeartbeatResponseDto> updateHeartbeat(
            @PathVariable UUID principalId,
            @Valid @RequestBody UpdateHeartbeatRequestDto request) {
        var command = principalMapper.toCommand(request, principalId);
        var updated = principalService.updateHeartbeat(command);
        var response = principalMapper.toResponseDto(updated);
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

    /**
     * Deactivate a principal.
     *
     * Requires permission: iam.principal:deactivate
     *
     * @param principalId the principal ID
     * @param request the deactivation request DTO
     * @return response DTO containing the principal_id and status
     */
    @PostMapping("/{principalId}/deactivate")
    public ResponseEntity<DeactivatePrincipalResponseDto> deactivatePrincipal(
            @PathVariable UUID principalId,
            @Valid @RequestBody DeactivatePrincipalRequestDto request) {
        var command = principalMapper.toCommand(request, principalId);
        var deactivated = principalService.deactivatePrincipal(command);
        var response = principalMapper.toResponseDto(deactivated);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a principal per GDPR right to erasure.
     *
     * Requires permission: iam.principal.gdpr:delete
     *
     * @param principalId the principal ID
     * @param request the GDPR deletion request DTO
     * @return response DTO containing the deletion result
     */
    @DeleteMapping("/{principalId}/gdpr")
    public ResponseEntity<DeletePrincipalGdprResponseDto> deletePrincipalGdpr(
            @PathVariable UUID principalId,
            @Valid @RequestBody DeletePrincipalGdprRequestDto request) {
        var command = principalMapper.toCommand(request, principalId);
        var deleted = principalService.deletePrincipalGdpr(command);
        var response = principalMapper.toResponseDto(deleted);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a principal by ID.
     *
     * Requires permission: iam.principal:read
     *
     * @param principalId the principal ID
     * @return response DTO containing the principal details
     */
    @GetMapping("/{principalId}")
    public ResponseEntity<GetPrincipalResponseDto> getPrincipal(@PathVariable UUID principalId) {
        var details = principalService.getPrincipalDetails(principalId);
        var response = principalMapper.toResponseDto(details);
        return ResponseEntity.ok(response);
    }

    /**
     * Update common attributes on a principal.
     *
     * Requires permission: iam.principal:update
     *
     * @param principalId the principal ID
     * @param request the update request DTO
     * @return response DTO containing the updated attributes
     */
    @PatchMapping("/{principalId}")
    public ResponseEntity<UpdateCommonAttributesResponseDto> updateCommonAttributes(
            @PathVariable UUID principalId,
            @Valid @RequestBody UpdateCommonAttributesRequestDto request) {
        var command = principalMapper.toCommand(request, principalId);
        var updated = principalService.updateCommonAttributes(command);
        var principal = principalService.findPrincipalById(principalId)
                .orElseThrow(() -> new RuntimeException("Principal not found: " + principalId));
        var response = principalMapper.toResponseDto(updated, principal);
        return ResponseEntity.ok(response);
    }

    /**
     * Search principals with filters.
     *
     * Requires permission: iam.principal:search
     *
     * @param search search term (partial match on username or email)
     * @param principalType filter by principal type
     * @param status filter by status
     * @param tenantId filter by tenant ID
     * @param page page number (1-indexed, default 1)
     * @param size page size (default 50, max 100)
     * @return paginated search results
     */
    @GetMapping
    public ResponseEntity<SearchPrincipalsResponseDto> searchPrincipals(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, name = "principal_type") String principalType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, name = "tenant_id") UUID tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {

        // Parse principal type if provided
        PrincipalType parsedPrincipalType = null;
        if (principalType != null && !principalType.isBlank()) {
            try {
                parsedPrincipalType = PrincipalType.valueOf(principalType.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid principal type, ignore filter
            }
        }

        // Parse status if provided
        PrincipalStatus parsedStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                parsedStatus = PrincipalStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore filter
            }
        }

        var query = new SearchPrincipalsQuery(search, parsedPrincipalType, parsedStatus, tenantId, page, size);
        var result = principalService.searchPrincipals(query);

        // Map to response DTO
        SearchPrincipalsResponseDto response = new SearchPrincipalsResponseDto();
        response.setTotal(result.total());
        response.setPage(result.page());
        response.setSize(result.size());

        var items = new ArrayList<SearchPrincipalsResponseDto.PrincipalSearchItem>();
        for (var item : result.items()) {
            var responseItem = new SearchPrincipalsResponseDto.PrincipalSearchItem();
            responseItem.setPrincipalId(item.principalId().toString());
            responseItem.setUsername(item.username());
            responseItem.setEmail(item.email());
            responseItem.setPrincipalType(item.principalType());
            responseItem.setStatus(item.status());
            responseItem.setPrimaryTenantId(item.primaryTenantId() != null ? item.primaryTenantId().toString() : null);
            responseItem.setLastLoginAt(item.lastLoginAt() != null ? item.lastLoginAt().toString() : null);
            responseItem.setCreatedAt(item.createdAt() != null ? item.createdAt().toString() : null);
            items.add(responseItem);
        }
        response.setItems(items);

        return ResponseEntity.ok(response);
    }
}
