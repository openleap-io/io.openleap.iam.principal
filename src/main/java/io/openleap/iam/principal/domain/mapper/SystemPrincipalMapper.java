package io.openleap.iam.principal.domain.mapper;

import io.openleap.iam.principal.domain.dto.CredentialStatus;
import io.openleap.iam.principal.domain.dto.SystemPrincipalCreated;
import io.openleap.iam.principal.domain.entity.SystemPrincipalEntity;
import io.openleap.iam.principal.domain.event.SystemPrincipalCreatedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SystemPrincipalMapper {

    @Mapping(target = "id", source = "businessId.value")
    @Mapping(target = "principalType", expression = "java(entity.getPrincipalType().name())")
    @Mapping(target = "credentialRotationDate", ignore = true)
    @Mapping(target = "daysUntilRotation", ignore = true)
    @Mapping(target = "lastRotatedAt", ignore = true)
    @Mapping(target = "rotationRequired", constant = "false")
    @Mapping(target = "hasApiKey", constant = "false")
    @Mapping(target = "hasCertificate", expression = "java(entity.getCertificateThumbprint() != null && !entity.getCertificateThumbprint().isBlank())")
    CredentialStatus toCredentialStatus(SystemPrincipalEntity entity);

    @Mapping(target = "principalId", source = "businessId.value")
    @Mapping(target = "principalType", expression = "java(entity.getPrincipalType().name())")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "systemIdentifier", source = "systemIdentifier")
    @Mapping(target = "integrationType", expression = "java(entity.getIntegrationType() != null ? entity.getIntegrationType().name() : null)")
    @Mapping(target = "primaryTenantId", source = "defaultTenantId")
    @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    @Mapping(target = "allowedOperations", source = "allowedOperations")
    @Mapping(target = "createdBy", source = "createdBy")
    SystemPrincipalCreatedEvent toSystemPrincipalCreatedEvent(SystemPrincipalEntity entity);

    @Mapping(target = "id", source = "businessId.value")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "systemIdentifier", source = "systemIdentifier")
    @Mapping(target = "integrationType", expression = "java(entity.getIntegrationType() != null ? entity.getIntegrationType().name() : null)")
    @Mapping(target = "certificateThumbprint", source = "certificateThumbprint")
    @Mapping(target = "allowedOperations", source = "allowedOperations")
    SystemPrincipalCreated toSystemPrincipalCreated(SystemPrincipalEntity entity);
}
