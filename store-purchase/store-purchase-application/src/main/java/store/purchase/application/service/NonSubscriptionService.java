package store.purchase.application.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import store.purchase.application.spi.PurchaseHandler;
import store.purchase.application.usecase.NonSubscriptionUseCase;
import store.purchase.domain.ProductData;
import store.purchase.domain.ProductPlatform;
import store.purchase.domain.ProductType;

import java.util.Objects;

@ApplicationScoped
public class NonSubscriptionService implements NonSubscriptionUseCase {
    private final Instance<PurchaseHandler<?,?>> handlers;

    @Inject
    public NonSubscriptionService(Instance<PurchaseHandler<?,?>> handlers) {
        this.handlers = handlers;
    }

    @Override
    public Uni<Boolean> handle(String userId, ProductData productData, String token) {
        if (productData.type() == ProductType.Subscription) {
            return Uni.createFrom().failure(new IllegalArgumentException("Product is a subscription"));
        }

        return resolveHandler(productData.platform())
                .handleNonSubscription(userId, productData, token)
                .map(Objects::nonNull);
    }

    private PurchaseHandler<?,?> resolveHandler(ProductPlatform platform) {
        for (PurchaseHandler<?,?> handler : handlers) {
            if (handler.supports(platform)) {
                return handler;
            }
        }

        throw new IllegalStateException("No purchase handler available for platform: " + platform);
    }
}
