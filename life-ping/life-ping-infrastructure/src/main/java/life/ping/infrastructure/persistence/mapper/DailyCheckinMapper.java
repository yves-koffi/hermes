package life.ping.infrastructure.persistence.mapper;

import life.ping.domain.model.DailyCheckin;
import life.ping.infrastructure.persistence.entity.DailyCheckinEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface DailyCheckinMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "localDate", source = "localDate")
    @Mapping(target = "checkedInAt", source = "checkedInAt")
    @Mapping(target = "source", source = "source")
    DailyCheckin toDomain(DailyCheckinEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "localDate", source = "localDate")
    @Mapping(target = "checkedInAt", source = "checkedInAt")
    @Mapping(target = "source", source = "source")
    DailyCheckinEntity toEntity(DailyCheckin domain);
}
