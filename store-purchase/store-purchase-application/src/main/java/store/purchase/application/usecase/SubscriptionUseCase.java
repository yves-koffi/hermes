package store.purchase.application.usecase;

import io.smallrye.mutiny.Uni;
import store.purchase.domain.ProductData;

public interface SubscriptionUseCase {
    Uni<Boolean> handle(
            String userId,
            ProductData productData,
            String token
    );
}
