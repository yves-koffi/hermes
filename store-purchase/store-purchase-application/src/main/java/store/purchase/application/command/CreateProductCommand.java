package store.purchase.application.command;

import store.purchase.domain.ProductPlatform;
import store.purchase.domain.ProductType;

public record CreateProductCommand(
        ProductType type,
        ProductPlatform platform,
        String externalProductId,
        String packageName,
        String bundleId,
        Long appAppleId
) {
}
