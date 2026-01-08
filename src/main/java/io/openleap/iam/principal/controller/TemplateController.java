package io.openleap.iam.principal.controller;

import io.openleap.iam.principal.controller.dto.HealthResponseDto;
import io.openleap.iam.principal.service.TemplateService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "LocalKeycloakOauth2")
@RestController
@RequestMapping("/api/v1/iam/principal")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HealthResponseDto> send() {
        return new ResponseEntity<>(
                templateService.checkHealth(), HttpStatus.OK);
    }
}
