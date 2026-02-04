package io.openleap.iam.principal.domain.mapper;

import io.openleap.iam.principal.domain.dto.DevicePrincipalCreated;
import io.openleap.iam.principal.domain.entity.DevicePrincipalEntity;
import io.openleap.iam.principal.domain.event.DevicePrincipalCreatedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DevicePrincipalMapper {

    @Mapping(target = "id", source = "businessId.value")
    DevicePrincipalCreated toDevicePrincipalCreated(DevicePrincipalEntity entity);

    @Mapping(target = "principalId", source = "businessId.value")
    @Mapping(target = "principalType", expression = "java(entity.getPrincipalType().name())")
    @Mapping(target = "deviceType", expression = "java(entity.getDeviceType() != null ? entity.getDeviceType().name() : null)")
    @Mapping(target = "primaryTenantId", source = "defaultTenantId")
    @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    DevicePrincipalCreatedEvent toDevicePrincipalCreatedEvent(DevicePrincipalEntity entity);
}
