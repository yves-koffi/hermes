package store.purchase.infrastructure.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import store.purchase.application.command.CreateProductCommand;
import store.purchase.application.command.UpdateProductCommand;
import store.purchase.application.result.ProductSummary;
import store.purchase.application.result.ProduitDetails;
import store.purchase.infrastructure.api.dto.CreateProductRequest;
import store.purchase.infrastructure.api.dto.ProductSummaryDto;
import store.purchase.infrastructure.api.dto.ProduitDetailsDto;
import store.purchase.infrastructure.api.dto.UpdateProductRequest;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface ProductApiMapper {

    CreateProductCommand toCreateCommand(CreateProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "externalProductId", ignore = true)
    UpdateProductCommand toUpdateCommand(UpdateProductRequest request);

    ProduitDetailsDto toDetails(ProduitDetails details);

    ProductSummaryDto toSummary(ProductSummary summary);

    List<ProductSummaryDto> toSummaryList(List<ProductSummary> products);
}
