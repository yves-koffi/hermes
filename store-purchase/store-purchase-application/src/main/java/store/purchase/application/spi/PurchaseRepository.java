package store.purchase.application.spi;

import io.smallrye.mutiny.Uni;
import store.purchase.domain.Purchase;

import java.util.Optional;

public interface PurchaseRepository {
    Uni<Purchase> createOrUpdate(Purchase purchase);
    Uni<Optional<Purchase>> findByOrderId(String orderId);
}
