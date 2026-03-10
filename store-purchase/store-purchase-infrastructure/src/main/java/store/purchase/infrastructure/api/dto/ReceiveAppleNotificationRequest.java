package store.purchase.infrastructure.api.dto;

import com.apple.itunes.storekit.model.ResponseBodyV2DecodedPayload;
import store.purchase.infrastructure.persistence.entity.ProductEntity;

public record ReceiveAppleNotificationRequest(
        ResponseBodyV2DecodedPayload payload,
        ProductEntity product
) {
}
