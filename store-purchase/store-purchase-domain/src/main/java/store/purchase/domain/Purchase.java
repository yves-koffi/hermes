package store.purchase.domain;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record Purchase(
        UUID id,
        IapSource iapSource,
        String orderId,
        String externalProductId,
        OffsetDateTime purchaseDate,
        OffsetDateTime expiryDate,
        PurchaseType purchaseType,
        PurchaseStatus status,
        UUID accountId,
        OffsetDateTime deletedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public Purchase {
        Objects.requireNonNull(purchaseType, "purchaseType must not be null");
        Objects.requireNonNull(status, "status must not be null");

        if (!status.supports(purchaseType)) {
            throw new IllegalArgumentException(
                    "Invalid status " + status + " for purchase type " + purchaseType
            );
        }
    }

}
