package store.purchase.application.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import store.purchase.application.spi.PurchaseHandler;
import store.purchase.application.usecase.VerifyPurchaseUseCase;
import store.purchase.domain.ProductData;
import store.purchase.domain.ProductPlatform;

@ApplicationScoped
public class VerifyPurchaseService implements VerifyPurchaseUseCase {

    @Inject
    Instance<PurchaseHandler<?, ?>> handlers;

    @Override
    public Uni<Boolean> handle(String userId, ProductData productData, String token) {
        return resolveHandler(productData.platform()).verifyPurchase(userId, productData, token);
    }

    private PurchaseHandler<?, ?> resolveHandler(ProductPlatform platform) {
        for (PurchaseHandler<?, ?> handler : handlers) {
            if (handler.supports(platform)) {
                return handler;
            }
        }

        throw new IllegalStateException("No purchase handler available for platform: " + platform);
    }
}
