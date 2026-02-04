package io.openleap.iam.principal.domain.mapper;

import io.openleap.iam.principal.domain.dto.CredentialStatus;
import io.openleap.iam.principal.domain.dto.CredentialsRotated;
import io.openleap.iam.principal.domain.dto.RotateCredentialsCommand;
import io.openleap.iam.principal.domain.dto.ServicePrincipalCreated;
import io.openleap.iam.principal.domain.entity.ServicePrincipalEntity;
import io.openleap.iam.principal.domain.event.CredentialsRotatedEvent;
import io.openleap.iam.principal.domain.event.ServicePrincipalCreatedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Mapper(componentModel = "spring")
public interface ServicePrincipalMapper {

    @Mapping(target = "id", source = "businessId.value")
    @Mapping(target = "principalType", expression = "java(entity.getPrincipalType().name())")
    @Mapping(target = "lastRotatedAt", source = "rotatedAt")
    @Mapping(target = "daysUntilRotation", expression = "java(calculateDaysUntilRotation(entity.getCredentialRotationDate()))")
    @Mapping(target = "rotationRequired", expression = "java(isRotationRequired(entity.getCredentialRotationDate()))")
    @Mapping(target = "hasApiKey", expression = "java(entity.getApiKeyHash() != null && !entity.getApiKeyHash().isBlank())")
    @Mapping(target = "hasCertificate", constant = "false")
    CredentialStatus toCredentialStatus(ServicePrincipalEntity entity);

    default Long calculateDaysUntilRotation(LocalDate credentialRotationDate) {
        if (credentialRotationDate == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), credentialRotationDate);
    }

    default Boolean isRotationRequired(LocalDate credentialRotationDate) {
        if (credentialRotationDate == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), credentialRotationDate) < 0;
    }

    @Mapping(target = "principalId", source = "entity.businessId.value")
    @Mapping(target = "principalType", expression = "java(entity.getPrincipalType().name())")
    @Mapping(target = "username", source = "entity.username")
    @Mapping(target = "serviceName", source = "entity.serviceName")
    @Mapping(target = "primaryTenantId", source = "entity.defaultTenantId")
    @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    @Mapping(target = "allowedScopes", source = "entity.allowedScopes")
    @Mapping(target = "credentialRotationDate", source = "entity.credentialRotationDate")
    @Mapping(target = "createdBy", source = "entity.createdBy")
    ServicePrincipalCreatedEvent toServicePrincipalCreatedEvent(ServicePrincipalEntity entity);

    @Mapping(target = "id", source = "entity.businessId.value")
    @Mapping(target = "username", source = "entity.username")
    @Mapping(target = "serviceName", source = "entity.serviceName")
    @Mapping(target = "apiKey", source = "apiKey")
    @Mapping(target = "keycloakClientId", source = "keycloakClientId")
    @Mapping(target = "keycloakClientSecret", source = "keycloakClientSecret")
    @Mapping(target = "allowedScopes", source = "entity.allowedScopes")
    @Mapping(target = "credentialRotationDate", source = "entity.credentialRotationDate")
    ServicePrincipalCreated toServicePrincipalCreated(ServicePrincipalEntity entity, String apiKey, String keycloakClientId, String keycloakClientSecret);

    @Mapping(target = "principalId", source = "entity.businessId.value")
    @Mapping(target = "serviceName", source = "entity.serviceName")
    @Mapping(target = "credentialRotationDate", source = "newRotationDate")
    @Mapping(target = "rotatedAt", source = "rotatedAt")
    @Mapping(target = "reason", source = "command.reason")
    CredentialsRotatedEvent toCredentialsRotatedEvent(ServicePrincipalEntity entity, RotateCredentialsCommand command, LocalDate newRotationDate, Instant rotatedAt);

    @Mapping(target = "id", source = "entity.businessId.value")
    @Mapping(target = "apiKey", source = "newApiKey")
    @Mapping(target = "keycloakClientSecret", source = "newKeycloakClientSecret")
    @Mapping(target = "credentialRotationDate", source = "newRotationDate")
    @Mapping(target = "rotatedAt", source = "rotatedAt")
    CredentialsRotated toCredentialsRotated(ServicePrincipalEntity entity, String newApiKey, String newKeycloakClientSecret, LocalDate newRotationDate, Instant rotatedAt);
}
