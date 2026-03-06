package store.purchase.domain;

import java.util.UUID;

public record ProductData(
        UUID id,
        ProductType type,
        ProductPlatform platform,
        String externalProductId,
        String packageName,
        String bundleId,
        Long appAppleId
) {
}
