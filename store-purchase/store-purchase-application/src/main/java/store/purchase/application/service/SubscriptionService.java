package store.purchase.application.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import store.purchase.application.spi.PurchaseHandler;
import store.purchase.application.usecase.SubscriptionUseCase;
import store.purchase.domain.ProductData;
import store.purchase.domain.ProductPlatform;
import store.purchase.domain.ProductType;

import java.util.Objects;

@ApplicationScoped
public class SubscriptionService implements SubscriptionUseCase {
    private final Instance<PurchaseHandler<?,?>> handlers;

    @Inject
    public SubscriptionService(Instance<PurchaseHandler<?,?>> handlers) {
        this.handlers = handlers;
    }

    @Override
    public Uni<Boolean> handle(String userId, ProductData productData, String token) {
        if (productData.type() != ProductType.Subscription) {
            return Uni.createFrom().failure(new IllegalArgumentException("Product is not a subscription"));
        }

        return resolveHandler(productData.platform())
                .handleSubscription(userId, productData, token)
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
