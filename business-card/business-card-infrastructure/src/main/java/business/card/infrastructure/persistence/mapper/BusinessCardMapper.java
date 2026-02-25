package business.card.infrastructure.persistence.mapper;

import business.card.domain.model.BusinessCard;
import business.card.infrastructure.persistence.entity.BusinessCardEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "cdi")
public interface BusinessCardMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "accountId", source = "accountId")
    @Mapping(target = "raw", source = "raw")
    @Mapping(target = "uid", source = "uid")
    @Mapping(target = "avatarUrl", source = "avatarUrl")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "softDeletedAt", source = "softDeletedAt")
    @Mapping(target = "saveAt", source = "saveAt")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    BusinessCard toDomain(BusinessCardEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "uid", source = "uid")
    @Mapping(target = "accountId", source = "accountId")
    @Mapping(target = "raw", source = "raw")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "avatarUrl", source = "avatarUrl")
    @Mapping(target = "softDeletedAt", source = "softDeletedAt")
    @Mapping(target = "saveAt", source = "saveAt")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    BusinessCardEntity toEntity(BusinessCard domain);

    default LocalDateTime map(OffsetDateTime value) {
        return value == null ? null : value.toLocalDateTime();
    }

    default OffsetDateTime map(LocalDateTime value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }
}
