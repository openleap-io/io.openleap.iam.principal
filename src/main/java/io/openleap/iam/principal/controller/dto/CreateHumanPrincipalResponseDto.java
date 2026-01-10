package io.openleap.iam.principal.controller.dto;

import java.util.UUID;

public class CreateHumanPrincipalResponseDto {
    
    private UUID principalId;
    
    public CreateHumanPrincipalResponseDto() {
    }
    
    public CreateHumanPrincipalResponseDto(UUID principalId) {
        this.principalId = principalId;
    }
    
    public UUID getPrincipalId() {
        return principalId;
    }
    
    public void setPrincipalId(UUID principalId) {
        this.principalId = principalId;
    }
}

