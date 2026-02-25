package life.ping.infrastructure.persistence.mapper;

import life.ping.domain.model.MissedDayMarker;
import life.ping.infrastructure.persistence.entity.MissedDayMarkerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface MissedDayMarkerMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "localDate", source = "localDate")
    @Mapping(target = "createdAt", source = "createdAt")
    MissedDayMarker toDomain(MissedDayMarkerEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "localDate", source = "localDate")
    @Mapping(target = "createdAt", source = "createdAt")
    MissedDayMarkerEntity toEntity(MissedDayMarker domain);
}
