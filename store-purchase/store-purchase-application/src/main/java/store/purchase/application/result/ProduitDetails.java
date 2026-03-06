package store.purchase.application.result;

import store.purchase.domain.ProductPlatform;
import store.purchase.domain.ProductType;

import java.util.UUID;

public record ProduitDetails(
        UUID id,
        ProductType type,
        ProductPlatform platform,
        String externalProductId,
        String packageName,
        String bundleId,
        Long appAppleId
) {
}
