package store.purchase.infrastructure.api.dto;

import store.purchase.domain.ProductPlatform;
import store.purchase.domain.ProductType;

import java.util.UUID;

public record ProductSummaryDto(
        UUID id,
        ProductType type,
        ProductPlatform platform,
        String externalProductId,
        String packageName
) {
}
