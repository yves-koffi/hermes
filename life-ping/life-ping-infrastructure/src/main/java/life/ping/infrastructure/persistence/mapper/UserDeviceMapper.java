package life.ping.infrastructure.persistence.mapper;

import life.ping.domain.model.UserDevice;
import life.ping.infrastructure.persistence.entity.UserDeviceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface UserDeviceMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "platform", source = "platform")
    @Mapping(target = "fcmToken", source = "fcmToken")
    @Mapping(target = "lastSeenAt", source = "lastSeenAt")
    UserDevice toDomain(UserDeviceEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "platform", source = "platform")
    @Mapping(target = "fcmToken", source = "fcmToken")
    @Mapping(target = "lastSeenAt", source = "lastSeenAt")
    UserDeviceEntity toEntity(UserDevice domain);
}
