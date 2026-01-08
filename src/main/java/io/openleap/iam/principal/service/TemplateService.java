package io.openleap.iam.principal.service;


import io.openleap.iam.principal.controller.dto.HealthResponseDto;
import org.springframework.stereotype.Service;

@Service
public class TemplateService {
    public HealthResponseDto checkHealth() {
        return new HealthResponseDto("healthy");
    }
}