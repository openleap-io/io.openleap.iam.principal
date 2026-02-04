package io.openleap.iam.principal.domain.mapper;

import io.openleap.iam.principal.domain.dto.DeactivatePrincipalCommand;
import io.openleap.iam.principal.domain.dto.DeletePrincipalGdprCommand;
import io.openleap.iam.principal.domain.dto.SuspendPrincipalCommand;
import io.openleap.iam.principal.domain.entity.Principal;
import io.openleap.iam.principal.domain.event.PrincipalDeactivatedEvent;
import io.openleap.iam.principal.domain.event.PrincipalDeletedEvent;
import io.openleap.iam.principal.domain.event.PrincipalSuspendedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface PrincipalEventMapper {

    @Mapping(target = "principalId", source = "principal.businessId.value")
    @Mapping(target = "principalType", expression = "java(principal.getPrincipalType().name())")
    @Mapping(target = "status", expression = "java(principal.getStatus().name())")
    @Mapping(target = "reason", source = "command.reason")
    @Mapping(target = "incidentTicket", source = "command.incidentTicket")
    PrincipalSuspendedEvent toPrincipalSuspendedEvent(Principal principal, SuspendPrincipalCommand command);

    @Mapping(target = "principalId", source = "principal.businessId.value")
    @Mapping(target = "principalType", expression = "java(principal.getPrincipalType().name())")
    @Mapping(target = "status", expression = "java(principal.getStatus().name())")
    @Mapping(target = "reason", source = "command.reason")
    @Mapping(target = "effectiveDate", source = "command.effectiveDate")
    PrincipalDeactivatedEvent toPrincipalDeactivatedEvent(Principal principal, DeactivatePrincipalCommand command);

    @Mapping(target = "principalId", source = "principal.businessId.value")
    @Mapping(target = "principalType", source = "principalType")
    @Mapping(target = "gdprRequestTicket", source = "command.gdprRequestTicket")
    @Mapping(target = "requestorEmail", source = "command.requestorEmail")
    @Mapping(target = "auditReference", source = "auditReference")
    @Mapping(target = "deletedAt", source = "deletedAt")
    PrincipalDeletedEvent toPrincipalDeletedEvent(Principal principal, DeletePrincipalGdprCommand command, String principalType, String auditReference, Instant deletedAt);
}
