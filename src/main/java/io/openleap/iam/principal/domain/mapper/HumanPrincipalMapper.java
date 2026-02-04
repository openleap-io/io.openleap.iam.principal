package io.openleap.iam.principal.domain.mapper;

import io.openleap.iam.principal.domain.dto.HumanPrincipalCreated;
import io.openleap.iam.principal.domain.dto.ProfileDetails;
import io.openleap.iam.principal.domain.entity.HumanPrincipalEntity;
import io.openleap.iam.principal.domain.event.PrincipalCreatedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HumanPrincipalMapper {

    @Mapping(target = "id", source = "businessId.value")
    @Mapping(target = "principalType", expression = "java(entity.getPrincipalType().name())")
    @Mapping(target = "status", expression = "java(entity.getStatus() != null ? entity.getStatus().name() : null)")
    @Mapping(target = "syncStatus", expression = "java(entity.getSyncStatus() != null ? entity.getSyncStatus().name() : null)")
    HumanPrincipalCreated toHumanPrincipalCreated(HumanPrincipalEntity entity);

    @Mapping(target = "principalId", source = "businessId.value")
    @Mapping(target = "principalType", expression = "java(entity.getPrincipalType().name())")
    @Mapping(target = "primaryTenantId", source = "defaultTenantId")
    @Mapping(target = "status", expression = "java(entity.getStatus() != null ? entity.getStatus().name() : null)")
    PrincipalCreatedEvent toPrincipalCreatedEvent(HumanPrincipalEntity entity);

    @Mapping(target = "id", source = "businessId.value")
    ProfileDetails toProfileDetails(HumanPrincipalEntity entity);
}
