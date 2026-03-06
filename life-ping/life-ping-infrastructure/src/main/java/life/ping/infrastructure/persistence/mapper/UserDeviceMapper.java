package life.ping.infrastructure.persistence.mapper;

import life.ping.domain.model.UserDevice;
import life.ping.infrastructure.persistence.entity.UserDeviceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface UserDeviceMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "accountId", source = "accountId")
    @Mapping(target = "platform", source = "platform")
    @Mapping(target = "fcmToken", source = "fcmToken")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "lastSeenAt", source = "lastSeenAt")
    @Mapping(target = "softDeletedAt", source = "softDeletedAt")
    UserDevice toDomain(UserDeviceEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "accountId", source = "accountId")
    @Mapping(target = "platform", source = "platform")
    @Mapping(target = "fcmToken", source = "fcmToken")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "lastSeenAt", source = "lastSeenAt")
    @Mapping(target = "softDeletedAt", source = "softDeletedAt")
    UserDeviceEntity toEntity(UserDevice domain);
}
