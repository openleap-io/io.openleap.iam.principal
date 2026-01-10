package io.openleap.iam.principal.service;

import io.openleap.iam.principal.domain.dto.CreateHumanPrincipalCommand;
import io.openleap.iam.principal.domain.dto.HumanPrincipalCreated;
import io.openleap.iam.principal.domain.entity.HumanPrincipalEntity;
import io.openleap.iam.principal.domain.entity.PrincipalStatus;
import io.openleap.iam.principal.domain.entity.PrincipalTenantMembershipEntity;
import io.openleap.iam.principal.domain.entity.PrincipalType;
import io.openleap.iam.principal.domain.entity.SyncStatus;
import io.openleap.iam.principal.exception.EmailAlreadyExistsException;
import io.openleap.iam.principal.exception.InactivePrincipalFoundException;
import io.openleap.iam.principal.exception.TenantNotFoundException;
import io.openleap.iam.principal.exception.UsernameAlreadyExistsException;
import io.openleap.iam.principal.repository.HumanPrincipalRepository;
import io.openleap.iam.principal.repository.PrincipalTenantMembershipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class HumanPrincipalService {
    
    private final HumanPrincipalRepository humanPrincipalRepository;
    private final PrincipalTenantMembershipRepository membershipRepository;
    private final TenantService tenantService;
    
    public HumanPrincipalService(
            HumanPrincipalRepository humanPrincipalRepository,
            PrincipalTenantMembershipRepository membershipRepository,
            TenantService tenantService) {
        this.humanPrincipalRepository = humanPrincipalRepository;
        this.membershipRepository = membershipRepository;
        this.tenantService = tenantService;
    }

    @Transactional
    public HumanPrincipalCreated createHumanPrincipal(CreateHumanPrincipalCommand command) {
        // Validate username uniqueness
        if (humanPrincipalRepository.existsByUsername(command.username())) {
            throw new UsernameAlreadyExistsException(command.username());
        }
        
        // Validate email uniqueness
        // Check for inactive principal with same email (Alt-1)
        var inactivePrincipal = humanPrincipalRepository.findInactiveByEmail(command.email());
        if (inactivePrincipal.isPresent()) {
            throw new InactivePrincipalFoundException(
                inactivePrincipal.get().getPrincipalId(), 
                command.email()
            );
        }
        
        // Check if email already exists (active principal)
        if (humanPrincipalRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException(command.email());
        }
        
        // Validate primary tenant exists
        if (!tenantService.tenantExists(command.primaryTenantId())) {
            throw new TenantNotFoundException(command.primaryTenantId());
        }
        
        // Create HumanPrincipalEntity
        HumanPrincipalEntity principal = new HumanPrincipalEntity();
        principal.setUsername(command.username());
        principal.setEmail(command.email());
        principal.setPrimaryTenantId(command.primaryTenantId());
        principal.setStatus(PrincipalStatus.PENDING);
        principal.setSyncStatus(SyncStatus.PENDING);
        principal.setContextTags(command.contextTags());
        
        // Set profile fields with defaults if not provided
        if (command.displayName() != null && !command.displayName().isBlank()) {
            principal.setDisplayName(command.displayName());
        } else {
            // Default display name from username
            principal.setDisplayName(command.username());
        }
        
        principal.setPhone(command.phone());
        principal.setLanguage(command.language());
        principal.setTimezone(command.timezone());
        principal.setLocale(command.locale());
        principal.setAvatarUrl(command.avatarUrl());
        principal.setBio(command.bio());
        principal.setPreferences(command.preferences());
        
        // Save principal
        principal = humanPrincipalRepository.save(principal);
        
        // Create PrincipalTenantMembership for primary tenant
        PrincipalTenantMembershipEntity membership = new PrincipalTenantMembershipEntity();
        membership.setPrincipalId(principal.getPrincipalId());
        membership.setPrincipalType(PrincipalType.HUMAN);
        membership.setTenantId(command.primaryTenantId());
        membership.setValidFrom(LocalDate.now());
        membership.setStatus(io.openleap.iam.principal.domain.entity.MembershipStatus.ACTIVE);
        
        membershipRepository.save(membership);
        
        return new HumanPrincipalCreated(principal.getPrincipalId());
    }
}
