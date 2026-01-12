package io.openleap.iam.principal.controller;

import io.openleap.iam.principal.controller.dto.ActivatePrincipalRequestDto;
import io.openleap.iam.principal.controller.dto.ActivatePrincipalResponseDto;
import io.openleap.iam.principal.controller.dto.CreateHumanPrincipalRequestDto;
import io.openleap.iam.principal.controller.dto.CreateHumanPrincipalResponseDto;
import io.openleap.iam.principal.controller.dto.DeactivatePrincipalRequestDto;
import io.openleap.iam.principal.controller.dto.DeactivatePrincipalResponseDto;
import io.openleap.iam.principal.controller.dto.DeletePrincipalGdprRequestDto;
import io.openleap.iam.principal.controller.dto.DeletePrincipalGdprResponseDto;
import io.openleap.iam.principal.controller.dto.SearchPrincipalsResponseDto;
import io.openleap.iam.principal.controller.dto.SuspendPrincipalRequestDto;
import io.openleap.iam.principal.controller.dto.SuspendPrincipalResponseDto;
import io.openleap.iam.principal.controller.dto.UpdateProfileRequestDto;
import io.openleap.iam.principal.controller.dto.UpdateProfileResponseDto;
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
