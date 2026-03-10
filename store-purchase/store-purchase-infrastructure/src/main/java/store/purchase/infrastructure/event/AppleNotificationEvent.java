package store.purchase.infrastructure.event;

import com.apple.itunes.storekit.model.ResponseBodyV2DecodedPayload;
import store.purchase.infrastructure.persistence.entity.ProductEntity;

public record AppleNotificationEvent(
        ResponseBodyV2DecodedPayload payload,
        ProductEntity product
) {
}
