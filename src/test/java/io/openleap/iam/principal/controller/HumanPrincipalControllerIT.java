package io.openleap.iam.principal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.openleap.iam.principal.controller.dto.*;
import io.openleap.iam.principal.controller.exception.PrincipalExceptionHandler;
import io.openleap.iam.principal.controller.mapper.PrincipalMapper;
import io.openleap.iam.principal.domain.dto.*;
import io.openleap.iam.principal.domain.entity.HumanPrincipalEntity;
import io.openleap.iam.principal.domain.entity.PrincipalStatus;
import io.openleap.iam.principal.exception.EmailAlreadyExistsException;
import io.openleap.iam.principal.exception.TenantNotFoundException;
import io.openleap.iam.principal.exception.UsernameAlreadyExistsException;
import io.openleap.iam.principal.repository.HumanPrincipalRepository;
import io.openleap.iam.principal.service.HumanPrincipalService;
import io.openleap.iam.principal.service.PrincipalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HumanPrincipalController.class)
@Import(PrincipalExceptionHandler.class)
@DisplayName("HumanPrincipalController Integration Tests")
class HumanPrincipalControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HumanPrincipalService humanPrincipalService;

    @MockBean
    private PrincipalService principalService;

    @MockBean
    private HumanPrincipalRepository humanPrincipalRepository;

    @MockBean
    private PrincipalMapper principalMapper;

    private static final String BASE_URL = "/api/v1/iam/principals";

    @Nested
    @DisplayName("POST /api/v1/iam/principals - Create Human Principal")
    class CreateHumanPrincipal {

        @Test
        @WithMockUser
        @DisplayName("should create human principal successfully")
        void shouldCreateHumanPrincipalSuccessfully() throws Exception {
            // given
            UUID principalId = UUID.randomUUID();
            UUID tenantId = UUID.randomUUID();

            CreateHumanPrincipalRequestDto request = new CreateHumanPrincipalRequestDto();
            request.setUsername("johndoe");
            request.setEmail("john@example.com");
            request.setFirstName("John");
            request.setLastName("Doe");
            request.setPrimaryTenantId(tenantId);

            CreateHumanPrincipalCommand command = new CreateHumanPrincipalCommand(
                    "johndoe", "john@example.com", tenantId,
                    null, null, "John", "Doe",
                    null, null, null, null, null, null, null
            );

            HumanPrincipalCreated created = new HumanPrincipalCreated(principalId);

            CreateHumanPrincipalResponseDto responseDto = new CreateHumanPrincipalResponseDto();
            responseDto.setPrincipalId(principalId);

            when(principalMapper.toCommand(any(CreateHumanPrincipalRequestDto.class))).thenReturn(command);
            when(humanPrincipalService.createHumanPrincipal(command)).thenReturn(created);
            when(principalMapper.toResponseDto(created)).thenReturn(responseDto);

            // when / then
            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.principalId").value(principalId.toString()));
        }

        @Test
        @WithMockUser
        @DisplayName("should return CONFLICT when username already exists")
        void shouldReturnConflictWhenUsernameExists() throws Exception {
            // given
            CreateHumanPrincipalRequestDto request = new CreateHumanPrincipalRequestDto();
            request.setUsername("existinguser");
            request.setEmail("new@example.com");
            request.setFirstName("Test");
            request.setLastName("User");
            request.setPrimaryTenantId(UUID.randomUUID());

            when(principalMapper.toCommand(any(CreateHumanPrincipalRequestDto.class))).thenReturn(mock(CreateHumanPrincipalCommand.class));
            when(humanPrincipalService.createHumanPrincipal(any(CreateHumanPrincipalCommand.class)))
                    .thenThrow(new UsernameAlreadyExistsException("existinguser"));

            // when / then
            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("UsernameAlreadyExists"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return CONFLICT when email already exists")
        void shouldReturnConflictWhenEmailExists() throws Exception {
            // given
            CreateHumanPrincipalRequestDto request = new CreateHumanPrincipalRequestDto();
            request.setUsername("newuser");
            request.setEmail("existing@example.com");
            request.setFirstName("Test");
            request.setLastName("User");
            request.setPrimaryTenantId(UUID.randomUUID());

            when(principalMapper.toCommand(any(CreateHumanPrincipalRequestDto.class))).thenReturn(mock(CreateHumanPrincipalCommand.class));
            when(humanPrincipalService.createHumanPrincipal(any(CreateHumanPrincipalCommand.class)))
                    .thenThrow(new EmailAlreadyExistsException("existing@example.com"));

            // when / then
            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("EmailAlreadyExists"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return NOT_FOUND when tenant does not exist")
        void shouldReturnNotFoundWhenTenantNotExists() throws Exception {
            // given
            UUID tenantId = UUID.randomUUID();
            CreateHumanPrincipalRequestDto request = new CreateHumanPrincipalRequestDto();
            request.setUsername("newuser");
            request.setEmail("new@example.com");
            request.setFirstName("Test");
            request.setLastName("User");
            request.setPrimaryTenantId(tenantId);

            when(principalMapper.toCommand(any(CreateHumanPrincipalRequestDto.class))).thenReturn(mock(CreateHumanPrincipalCommand.class));
            when(humanPrincipalService.createHumanPrincipal(any(CreateHumanPrincipalCommand.class)))
                    .thenThrow(new TenantNotFoundException(tenantId));

            // when / then
            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("TenantNotFound"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/iam/principals/{principalId} - Get Principal")
    class GetPrincipal {

        @Test
        @WithMockUser
        @DisplayName("should return principal details successfully")
        void shouldReturnPrincipalDetails() throws Exception {
            // given
            UUID principalId = UUID.randomUUID();
            UUID tenantId = UUID.randomUUID();

            PrincipalDetails details = new PrincipalDetails(
                    principalId, "HUMAN", "johndoe", "john@example.com",
                    "ACTIVE", tenantId, Instant.now(), Instant.now(),
                    Map.of(), true, false, null,
                    "John Doe", "John", "Doe",
                    null, null, null, null, null, null, null, null, null, null
            );

            GetPrincipalResponseDto responseDto = new GetPrincipalResponseDto();
            responseDto.setPrincipalId(principalId.toString());
            responseDto.setUsername("johndoe");
            responseDto.setStatus("ACTIVE");

            when(principalService.getPrincipalDetails(principalId)).thenReturn(details);
            when(principalMapper.toResponseDto(details)).thenReturn(responseDto);

            // when / then
            mockMvc.perform(get(BASE_URL + "/{principalId}", principalId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.principalId").value(principalId.toString()))
                    .andExpect(jsonPath("$.username").value("johndoe"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 500 when principal not found")
        void shouldReturnErrorWhenPrincipalNotFound() throws Exception {
            // given
            UUID principalId = UUID.randomUUID();
            when(principalService.getPrincipalDetails(principalId))
                    .thenThrow(new RuntimeException("Principal not found"));

            // when / then
            mockMvc.perform(get(BASE_URL + "/{principalId}", principalId))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/iam/principals/{principalId}/profile - Update Profile")
    class UpdateProfile {

        @Test
        @WithMockUser
        @DisplayName("should update profile successfully")
        void shouldUpdateProfileSuccessfully() throws Exception {
            // given
            UUID principalId = UUID.randomUUID();

            UpdateProfileRequestDto request = new UpdateProfileRequestDto();
            request.setFirstName("UpdatedFirst");
            request.setLastName("UpdatedLast");

            UpdateProfileCommand command = new UpdateProfileCommand(
                    principalId, "UpdatedFirst", "UpdatedLast",
                    null, null, null, null, null, null, null, null, null
            );

            ProfileUpdated updated = new ProfileUpdated(principalId, List.of("first_name", "last_name"));

            HumanPrincipalEntity principal = new HumanPrincipalEntity();
            principal.setPrincipalId(principalId);
            principal.setFirstName("UpdatedFirst");
            principal.setLastName("UpdatedLast");

            UpdateProfileResponseDto responseDto = new UpdateProfileResponseDto();
            responseDto.setPrincipalId(principalId.toString());
            responseDto.setFirstName("UpdatedFirst");
            responseDto.setLastName("UpdatedLast");

            when(principalMapper.toCommand(any(UpdateProfileRequestDto.class), eq(principalId))).thenReturn(command);
            when(humanPrincipalService.updateProfile(command)).thenReturn(updated);
            when(humanPrincipalRepository.findById(principalId)).thenReturn(Optional.of(principal));
            when(principalMapper.toResponseDto(updated, principal)).thenReturn(responseDto);

            // when / then
            mockMvc.perform(patch(BASE_URL + "/{principalId}/profile", principalId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.principalId").value(principalId.toString()))
                    .andExpect(jsonPath("$.firstName").value("UpdatedFirst"))
                    .andExpect(jsonPath("$.lastName").value("UpdatedLast"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/iam/principals/{principalId}/profile - Get Profile")
    class GetProfile {

        @Test
        @WithMockUser
        @DisplayName("should return profile details successfully")
        void shouldReturnProfileDetails() throws Exception {
            // given
            UUID principalId = UUID.randomUUID();

            ProfileDetails details = new ProfileDetails(
                    principalId, "John", "Doe", "Johnny",
                    "+1234567890", "en", "America/New_York", "en-US",
                    "https://avatar.com/img.jpg", "Bio text",
                    Map.of("theme", "dark")
            );

            GetProfileResponseDto responseDto = new GetProfileResponseDto();
            responseDto.setPrincipalId(principalId.toString());
            responseDto.setFirstName("John");
            responseDto.setLastName("Doe");
            responseDto.setDisplayName("Johnny");

            when(humanPrincipalService.getProfile(principalId)).thenReturn(details);
            when(principalMapper.toResponseDto(details)).thenReturn(responseDto);

            // when / then
            mockMvc.perform(get(BASE_URL + "/{principalId}/profile", principalId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.principalId").value(principalId.toString()))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"))
                    .andExpect(jsonPath("$.displayName").value("Johnny"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/iam/principals/{principalId}/activate - Activate Principal")
    class ActivatePrincipal {

        @Test
        @WithMockUser
        @DisplayName("should activate principal successfully")
        void shouldActivatePrincipalSuccessfully() throws Exception {
            // given
            UUID principalId = UUID.randomUUID();

            ActivatePrincipalRequestDto request = new ActivatePrincipalRequestDto();
            request.setAdminOverride(true);
            request.setReason("Admin activation");

            ActivatePrincipalCommand command = new ActivatePrincipalCommand(
                    principalId, null, true, "Admin activation"
            );

            PrincipalActivated activated = new PrincipalActivated(principalId);

            ActivatePrincipalResponseDto responseDto = new ActivatePrincipalResponseDto();
            responseDto.setPrincipalId(principalId.toString());
            responseDto.setStatus("ACTIVE");

            when(principalMapper.toCommand(any(ActivatePrincipalRequestDto.class), eq(principalId))).thenReturn(command);
            when(principalService.activatePrincipal(command)).thenReturn(activated);
            when(principalMapper.toResponseDto(activated)).thenReturn(responseDto);

            // when / then
            mockMvc.perform(post(BASE_URL + "/{principalId}/activate", principalId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.principalId").value(principalId.toString()))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/iam/principals/{principalId}/suspend - Suspend Principal")
    class SuspendPrincipal {

        @Test
        @WithMockUser
        @DisplayName("should suspend principal successfully")
        void shouldSuspendPrincipalSuccessfully() throws Exception {
            // given
            UUID principalId = UUID.randomUUID();

            SuspendPrincipalRequestDto request = new SuspendPrincipalRequestDto();
            request.setReason("Security concern");
            request.setIncidentTicket("INC-12345");

            SuspendPrincipalCommand command = new SuspendPrincipalCommand(
                    principalId, "Security concern", "INC-12345"
            );

            PrincipalSuspended suspended = new PrincipalSuspended(principalId);

            SuspendPrincipalResponseDto responseDto = new SuspendPrincipalResponseDto();
            responseDto.setPrincipalId(principalId.toString());
            responseDto.setStatus("SUSPENDED");

            when(principalMapper.toCommand(any(SuspendPrincipalRequestDto.class), eq(principalId))).thenReturn(command);
            when(principalService.suspendPrincipal(command)).thenReturn(suspended);
            when(principalMapper.toResponseDto(suspended)).thenReturn(responseDto);

            // when / then
            mockMvc.perform(post(BASE_URL + "/{principalId}/suspend", principalId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.principalId").value(principalId.toString()))
                    .andExpect(jsonPath("$.status").value("SUSPENDED"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/iam/principals/{principalId}/deactivate - Deactivate Principal")
    class DeactivatePrincipal {

        @Test
        @WithMockUser
        @DisplayName("should deactivate principal successfully")
        void shouldDeactivatePrincipalSuccessfully() throws Exception {
            // given
            UUID principalId = UUID.randomUUID();

            DeactivatePrincipalRequestDto request = new DeactivatePrincipalRequestDto();
            request.setReason("Left company");
            request.setEffectiveDate(LocalDate.now());

            DeactivatePrincipalCommand command = new DeactivatePrincipalCommand(
                    principalId, "Left company", LocalDate.now()
            );

            PrincipalDeactivated deactivated = new PrincipalDeactivated(principalId);

            DeactivatePrincipalResponseDto responseDto = new DeactivatePrincipalResponseDto();
            responseDto.setPrincipalId(principalId.toString());
            responseDto.setStatus("INACTIVE");

            when(principalMapper.toCommand(any(DeactivatePrincipalRequestDto.class), eq(principalId))).thenReturn(command);
            when(principalService.deactivatePrincipal(command)).thenReturn(deactivated);
            when(principalMapper.toResponseDto(deactivated)).thenReturn(responseDto);

            // when / then
            mockMvc.perform(post(BASE_URL + "/{principalId}/deactivate", principalId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.principalId").value(principalId.toString()))
                    .andExpect(jsonPath("$.status").value("INACTIVE"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/iam/principals/{principalId}/tenants - List Tenant Memberships")
    class ListTenantMemberships {

        @Test
        @WithMockUser
        @DisplayName("should return tenant memberships successfully")
        void shouldReturnTenantMemberships() throws Exception {
            // given
            UUID principalId = UUID.randomUUID();
            UUID tenantId = UUID.randomUUID();

            TenantMembershipItem item = new TenantMembershipItem(
                    UUID.randomUUID(), principalId, tenantId,
                    LocalDate.now(), null, "ACTIVE", true
            );

            ListTenantMembershipsResult result = new ListTenantMembershipsResult(
                    List.of(item), 1, 1, 50
            );

            ListTenantMembershipsResponseDto responseDto = new ListTenantMembershipsResponseDto();
            responseDto.setTotal(1);
            responseDto.setPage(1);
            responseDto.setSize(50);

            when(principalService.listTenantMemberships(principalId, 1, 50)).thenReturn(result);
            when(principalMapper.toResponseDto(result)).thenReturn(responseDto);

            // when / then
            mockMvc.perform(get(BASE_URL + "/{principalId}/tenants", principalId)
                            .param("page", "1")
                            .param("size", "50"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(1))
                    .andExpect(jsonPath("$.page").value(1))
                    .andExpect(jsonPath("$.size").value(50));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/iam/principals - Search Principals")
    class SearchPrincipals {

        @Test
        @WithMockUser
        @DisplayName("should return search results successfully")
        void shouldReturnSearchResults() throws Exception {
            // given
            UUID principalId = UUID.randomUUID();
            UUID tenantId = UUID.randomUUID();

            SearchPrincipalsResult.PrincipalItem item = new SearchPrincipalsResult.PrincipalItem(
                    principalId, "johndoe", "john@example.com", "HUMAN",
                    "ACTIVE", tenantId, Instant.now(), Instant.now()
            );

            SearchPrincipalsResult result = new SearchPrincipalsResult(
                    List.of(item), 1, 1, 50
            );

            when(principalService.searchPrincipals(any(SearchPrincipalsQuery.class))).thenReturn(result);

            // when / then
            mockMvc.perform(get(BASE_URL)
                            .param("search", "john")
                            .param("page", "1")
                            .param("size", "50"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(1))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items[0].username").value("johndoe"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return empty results when no match")
        void shouldReturnEmptyResultsWhenNoMatch() throws Exception {
            // given
            SearchPrincipalsResult result = new SearchPrincipalsResult(
                    List.of(), 0, 1, 50
            );

            when(principalService.searchPrincipals(any(SearchPrincipalsQuery.class))).thenReturn(result);

            // when / then
            mockMvc.perform(get(BASE_URL)
                            .param("search", "nonexistent")
                            .param("page", "1")
                            .param("size", "50"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(0))
                    .andExpect(jsonPath("$.items").isEmpty());
        }
    }
}
