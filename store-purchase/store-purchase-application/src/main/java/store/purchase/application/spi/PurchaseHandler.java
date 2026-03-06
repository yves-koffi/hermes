package store.purchase.application.spi;

import io.smallrye.mutiny.Uni;
import store.purchase.domain.ProductData;
import store.purchase.domain.ProductPlatform;
import store.purchase.domain.Purchase;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface PurchaseHandler<T,R> {

    boolean supports(ProductPlatform platform);

    /**
     * Verifies a purchase.
     *
     * @param accountId      The ID of the user.
     * @param productData The data of the product.
     * @param token       The purchase token.
     * @return Uni<Boolean> if the purchase is valid, false otherwise.
     */
    Uni<Boolean> verifyPurchase(
            String accountId,
            ProductData productData,
            String token
    );

    /**
     * Verifies if a non-subscription purchase (aka consumable) is valid
     * and updates the database.
     *
     * @param accountId      The ID of the user.
     * @param productData The data of the product.
     * @param token       The purchase token.
     * @return Uni<Purchase> containing the persisted purchase when handled.
     */
    Uni<Purchase> handleNonSubscription(
            String accountId,
            ProductData productData,
            String token
    );

    /**
     * Verifies if a subscription purchase (aka non-consumable) is valid
     * and updates the database.
     *
     * @param accountId      The ID of the user.
     * @param productData The data of the product.
     * @param token       The purchase token.
     * @return Uni<Purchase> containing the persisted purchase when handled.
     */
    Uni<Purchase> handleSubscription(
            String accountId,
            ProductData productData,
            String token
    );

    Uni<Void> handlePullPurchase(R command);

    T getClient() throws GeneralSecurityException, IOException;
}
