package store.purchase.infrastructure.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import store.purchase.domain.ProductPlatform;
import store.purchase.domain.ProductType;

public record UpdateProductRequest(
        @NotNull(message = "type is required")
        ProductType type,
        @NotNull(message = "platform is required")
        ProductPlatform platform,
        @NotBlank(message = "packageName is required")
        String packageName,
        String bundleId,
        Long appAppleId
) {
}
