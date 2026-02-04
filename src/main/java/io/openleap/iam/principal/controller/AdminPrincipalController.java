package io.openleap.iam.principal.controller;

import io.openleap.iam.principal.controller.dto.CrossTenantSearchResponseDto;
import io.openleap.iam.principal.controller.mapper.PrincipalMapper;
import io.openleap.iam.principal.domain.dto.CrossTenantSearchQuery;
import io.openleap.iam.principal.service.PrincipalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin controller for cross-tenant principal operations.
 */
//TODO: Enable when cross-tenant principal search is implemented
//@RestController
//@RequestMapping("/api/v1/iam/admin/principals")
public class AdminPrincipalController {

    private final PrincipalService principalService;
    private final PrincipalMapper principalMapper;

    public AdminPrincipalController(PrincipalService principalService, PrincipalMapper principalMapper) {
        this.principalService = principalService;
        this.principalMapper = principalMapper;
    }

    /**
     * Search principals across all tenants.
     *
     * Requires permission: iam.admin:cross_tenant
     *
     * @param search search term (partial match on username or email)
     * @param principalType filter by principal type
     * @param status filter by status
     * @param page page number (1-indexed, default 1)
     * @param size page size (default 50, max 100)
     * @return paginated search results across all tenants
     */
//    @GetMapping
//    public ResponseEntity<CrossTenantSearchResponseDto> searchPrincipalsCrossTenant(
//            @RequestParam(required = false) String search,
//            @RequestParam(required = false, name = "principal_type") String principalType,
//            @RequestParam(required = false) String status,
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "50") int size) {
//
//        var query = new CrossTenantSearchQuery(search, principalType, status, page, size);
//        var result = principalService.searchPrincipalsCrossTenant(query);
//        var response = principalMapper.toResponseDto(result);
//
//        return ResponseEntity.ok(response);
//    }
}
