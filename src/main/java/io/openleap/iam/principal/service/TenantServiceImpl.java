package io.openleap.iam.principal.service;

import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class TenantServiceImpl implements TenantService {
    
    @Override
    public boolean tenantExists(UUID tenantId) {
        // TODO: Implement actual tenant service client call
        return true;
    }
}

