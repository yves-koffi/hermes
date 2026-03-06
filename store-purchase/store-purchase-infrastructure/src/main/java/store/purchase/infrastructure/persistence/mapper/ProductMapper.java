package store.purchase.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import store.purchase.application.command.CreateProductCommand;
import store.purchase.application.command.UpdateProductCommand;
import store.purchase.application.result.ProductSummary;
import store.purchase.application.result.ProduitDetails;
import store.purchase.domain.ProductData;
import store.purchase.infrastructure.persistence.entity.ProductEntity;

@Mapper(componentModel = "cdi")
public interface ProductMapper {

    ProductData toDomain(ProductEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProductEntity toEntity(ProductData domain);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(ProductData domain, @MappingTarget ProductEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProductEntity toEntity(CreateProductCommand command);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateProductCommand command, @MappingTarget ProductEntity entity);

    ProduitDetails toDetails(ProductEntity entity);

    ProductSummary toSummary(ProductEntity entity);
}
