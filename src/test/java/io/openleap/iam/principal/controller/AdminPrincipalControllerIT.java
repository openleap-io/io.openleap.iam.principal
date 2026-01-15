package io.openleap.iam.principal.controller;

import io.openleap.iam.principal.controller.dto.*;
import io.openleap.iam.principal.controller.exception.PrincipalExceptionHandler;
import io.openleap.iam.principal.controller.mapper.PrincipalMapper;
import io.openleap.iam.principal.domain.dto.*;
import io.openleap.iam.principal.service.PrincipalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminPrincipalController.class)
@Import(PrincipalExceptionHandler.class)
@DisplayName("AdminPrincipalController Integration Tests")
class AdminPrincipalControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PrincipalService principalService;

    @MockBean
    private PrincipalMapper principalMapper;

    private static final String BASE_URL = "/api/v1/iam/admin/principals";

    @Nested
    @DisplayName("GET /api/v1/iam/admin/principals - Cross-Tenant Search")
    class CrossTenantSearch {

        @Test
        @WithMockUser
        @DisplayName("should return search results successfully")
        void shouldReturnSearchResultsSuccessfully() throws Exception {
            // given
            UUID principalId = UUID.randomUUID();
            UUID tenantId = UUID.randomUUID();

            CrossTenantPrincipalItem item = new CrossTenantPrincipalItem(
                    principalId, "HUMAN", "johndoe", "john@example.com", "ACTIVE", tenantId
            );

            CrossTenantSearchResult result = new CrossTenantSearchResult(
                    List.of(item), 1, 1, 50
            );

            CrossTenantSearchResponseDto responseDto = new CrossTenantSearchResponseDto();
            responseDto.setTotal(1);
            responseDto.setPage(1);
            responseDto.setSize(50);

            CrossTenantSearchResponseDto.CrossTenantPrincipalItemDto itemDto =
                    new CrossTenantSearchResponseDto.CrossTenantPrincipalItemDto();
            itemDto.setPrincipalId(principalId.toString());
            itemDto.setUsername("johndoe");
            itemDto.setEmail("john@example.com");
            itemDto.setPrincipalType("HUMAN");
            itemDto.setStatus("ACTIVE");
            responseDto.setItems(List.of(itemDto));

            when(principalService.searchPrincipalsCrossTenant(any(CrossTenantSearchQuery.class))).thenReturn(result);
            when(principalMapper.toResponseDto(result)).thenReturn(responseDto);

            // when / then
            mockMvc.perform(get(BASE_URL)
                            .param("search", "john")
                            .param("page", "1")
                            .param("size", "50"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(1))
                    .andExpect(jsonPath("$.page").value(1))
                    .andExpect(jsonPath("$.size").value(50))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items[0].username").value("johndoe"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return empty results when no match")
        void shouldReturnEmptyResultsWhenNoMatch() throws Exception {
            // given
            CrossTenantSearchResult result = new CrossTenantSearchResult(
                    List.of(), 0, 1, 50
            );

            CrossTenantSearchResponseDto responseDto = new CrossTenantSearchResponseDto();
            responseDto.setTotal(0);
            responseDto.setPage(1);
            responseDto.setSize(50);
            responseDto.setItems(List.of());

            when(principalService.searchPrincipalsCrossTenant(any(CrossTenantSearchQuery.class))).thenReturn(result);
            when(principalMapper.toResponseDto(result)).thenReturn(responseDto);

            // when / then
            mockMvc.perform(get(BASE_URL)
                            .param("search", "nonexistent")
                            .param("page", "1")
                            .param("size", "50"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(0))
                    .andExpect(jsonPath("$.items").isEmpty());
        }

        @Test
        @WithMockUser
        @DisplayName("should filter by principal type")
        void shouldFilterByPrincipalType() throws Exception {
            // given
            CrossTenantSearchResult result = new CrossTenantSearchResult(
                    List.of(), 0, 1, 50
            );

            CrossTenantSearchResponseDto responseDto = new CrossTenantSearchResponseDto();
            responseDto.setTotal(0);
            responseDto.setPage(1);
            responseDto.setSize(50);
            responseDto.setItems(List.of());

            when(principalService.searchPrincipalsCrossTenant(any(CrossTenantSearchQuery.class))).thenReturn(result);
            when(principalMapper.toResponseDto(result)).thenReturn(responseDto);

            // when / then
            mockMvc.perform(get(BASE_URL)
                            .param("principal_type", "SERVICE")
                            .param("page", "1")
                            .param("size", "50"))
                    .andExpect(status().isOk());

            verify(principalService).searchPrincipalsCrossTenant(argThat(query ->
                    "SERVICE".equals(query.principalType())));
        }

        @Test
        @WithMockUser
        @DisplayName("should filter by status")
        void shouldFilterByStatus() throws Exception {
            // given
            CrossTenantSearchResult result = new CrossTenantSearchResult(
                    List.of(), 0, 1, 50
            );

            CrossTenantSearchResponseDto responseDto = new CrossTenantSearchResponseDto();
            responseDto.setTotal(0);
            responseDto.setPage(1);
            responseDto.setSize(50);
            responseDto.setItems(List.of());

            when(principalService.searchPrincipalsCrossTenant(any(CrossTenantSearchQuery.class))).thenReturn(result);
            when(principalMapper.toResponseDto(result)).thenReturn(responseDto);

            // when / then
            mockMvc.perform(get(BASE_URL)
                            .param("status", "ACTIVE")
                            .param("page", "1")
                            .param("size", "50"))
                    .andExpect(status().isOk());

            verify(principalService).searchPrincipalsCrossTenant(argThat(query ->
                    "ACTIVE".equals(query.status())));
        }

        @Test
        @WithMockUser
        @DisplayName("should use default pagination values")
        void shouldUseDefaultPaginationValues() throws Exception {
            // given
            CrossTenantSearchResult result = new CrossTenantSearchResult(
                    List.of(), 0, 1, 50
            );

            CrossTenantSearchResponseDto responseDto = new CrossTenantSearchResponseDto();
            responseDto.setTotal(0);
            responseDto.setPage(1);
            responseDto.setSize(50);
            responseDto.setItems(List.of());

            when(principalService.searchPrincipalsCrossTenant(any(CrossTenantSearchQuery.class))).thenReturn(result);
            when(principalMapper.toResponseDto(result)).thenReturn(responseDto);

            // when / then
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk());

            verify(principalService).searchPrincipalsCrossTenant(argThat(query ->
                    query.page() == 1 && query.size() == 50));
        }

        @Test
        @WithMockUser
        @DisplayName("should search with multiple filters")
        void shouldSearchWithMultipleFilters() throws Exception {
            // given
            CrossTenantSearchResult result = new CrossTenantSearchResult(
                    List.of(), 0, 1, 50
            );

            CrossTenantSearchResponseDto responseDto = new CrossTenantSearchResponseDto();
            responseDto.setTotal(0);
            responseDto.setPage(1);
            responseDto.setSize(50);
            responseDto.setItems(List.of());

            when(principalService.searchPrincipalsCrossTenant(any(CrossTenantSearchQuery.class))).thenReturn(result);
            when(principalMapper.toResponseDto(result)).thenReturn(responseDto);

            // when / then
            mockMvc.perform(get(BASE_URL)
                            .param("search", "test")
                            .param("principal_type", "HUMAN")
                            .param("status", "ACTIVE")
                            .param("page", "2")
                            .param("size", "25"))
                    .andExpect(status().isOk());

            verify(principalService).searchPrincipalsCrossTenant(argThat(query ->
                    "test".equals(query.search()) &&
                    "HUMAN".equals(query.principalType()) &&
                    "ACTIVE".equals(query.status()) &&
                    query.page() == 2 &&
                    query.size() == 25));
        }
    }
}
