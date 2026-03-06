package store.purchase.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import store.purchase.domain.Purchase;
import store.purchase.infrastructure.persistence.entity.PurchaseEntity;

import java.time.OffsetDateTime;

@Mapper(componentModel = "cdi")
public interface PurchaseMapper {

    @Mapping(target = "purchaseDate", expression = "java(toOffsetDateTime(entity.getPurchaseDate()))")
    @Mapping(target = "expiryDate", expression = "java(toOffsetDateTime(entity.getExpiryDate()))")
    Purchase toDomain(PurchaseEntity entity);

    @Mapping(target = "purchaseDate", expression = "java(toColumnValue(domain.purchaseDate()))")
    @Mapping(target = "expiryDate", expression = "java(toColumnValue(domain.expiryDate()))")
    PurchaseEntity toEntity(Purchase domain);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "purchaseDate", expression = "java(toColumnValue(domain.purchaseDate()))")
    @Mapping(target = "expiryDate", expression = "java(toColumnValue(domain.expiryDate()))")
    void updateEntity(Purchase domain, @MappingTarget PurchaseEntity entity);

    default String toColumnValue(OffsetDateTime value) {
        return value == null ? null : value.toString();
    }

    default OffsetDateTime toOffsetDateTime(String value) {
        return value == null ? null : OffsetDateTime.parse(value);
    }
}
