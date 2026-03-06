package store.purchase.infrastructure.store.google.play;


import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.model.ProductPurchase;
import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import store.purchase.application.command.ReceivePubsubMessageCommand;
import store.purchase.application.spi.PurchaseHandler;
import store.purchase.application.spi.PurchaseRepository;
import store.purchase.domain.*;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

@ApplicationScoped
public class GooglePlayPurchaseHandler implements PurchaseHandler<AndroidPublisher, ReceivePubsubMessageCommand> {
    private volatile AndroidPublisher client;

    @ConfigProperty(name = "google.client.serviceAccountPath")
    String serviceAccountResourcePath;
    @ConfigProperty(name = "google.server.app")
    String serverName;

    @Inject
    PurchaseRepository repository;

    @Override
    public boolean supports(ProductPlatform platform) {
        return platform == ProductPlatform.ANDROID;
    }

    @Override
    public Uni<Boolean> verifyPurchase(
            String accountId,
            ProductData productData,
            String token) {
        validateRequest(accountId, productData, token);

        if (productData.type() == ProductType.Subscription) {
            return this.handleSubscription(accountId, productData, token)
                    .map(Objects::nonNull);
        } else {
            return this.handleNonSubscription(accountId, productData, token)
                    .map(Objects::nonNull);
        }
    }

    @Override
    public Uni<Purchase> handleNonSubscription(
            String accountId,
            ProductData productData,
            String token
    ) {
        validateRequest(accountId, productData, token);

        return Uni.createFrom()
                .item(this.getClient())
                .map(client -> Unchecked.supplier(() -> executeProductPurchase(client, productData, token)).get())
                .chain(purchase -> persistNonSubscription(accountId, productData, token, purchase));
    }

    @Override
    public Uni<Purchase> handleSubscription(
            String accountId,
            ProductData productData,
            String token
    ) {
        validateRequest(accountId, productData, token);

        return Uni.createFrom()
                .item(this.getClient())
                .map(client -> Unchecked.supplier(() -> executeSubscriptionPurchase(client, productData, token)).get())
                .chain(purchase -> persistSubscription(accountId, productData, token, purchase));
    }


    @Override
    public Uni<Void> handlePullPurchase(ReceivePubsubMessageCommand command) {

        return Uni.createFrom().voidItem();
    }

    @Produces
    @Override
    public AndroidPublisher getClient() {
        if (client != null) {
            return client;
        }

        try {
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(serviceAccountResourcePath)))
                    .createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));

            client = new AndroidPublisher.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName(serverName)
                    .build();
            return client;
        } catch (IOException e) {
            throw new RuntimeException("Unable to initialize Google Play client", e);
        }
    }

    private ProductPurchase executeProductPurchase(
            AndroidPublisher client,
            ProductData productData,
            String token
    ) throws IOException {
        try {
            return client.purchases()
                    .products()
                    .get(productData.packageName(), productData.externalProductId(), token)
                    .execute();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la vérification de l'achat Google Play", e);
        }
    }

    private SubscriptionPurchase executeSubscriptionPurchase(
            AndroidPublisher client,
            ProductData productData,
            String token
    ) throws IOException {
        try {
            return client.purchases()
                    .subscriptions()
                    .get(productData.packageName(), productData.externalProductId(), token)
                    .execute();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la vérification de l'abonnement Google Play", e);
        }
    }

    private Uni<Purchase> persistNonSubscription(
            String accountId,
            ProductData productData,
            String token,
            ProductPurchase purchase
    ) {
        Integer purchaseState = purchase.getPurchaseState();
        PurchaseStatus status = switch (purchaseState == null ? -1 : purchaseState) {
            case 0 -> PurchaseStatus.COMPLETED;
            case 2 -> PurchaseStatus.PENDING;
            default -> PurchaseStatus.CANCELLED;
        };


        String acId = accountId != null ? accountId : purchase.getObfuscatedExternalAccountId();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        return repository.createOrUpdate(
                new Purchase(
                        null,
                        IapSource.GOOGLE_PLAY,
                        resolveOrderId(
                                purchase.getOrderId(),
                                accountId,
                                productData.externalProductId(),
                                token,
                                PurchaseType.NON_SUBSCRIPTION
                        ),
                        productData.externalProductId(),
                        toOffsetDateTime(purchase.getPurchaseTimeMillis(), now),
                        null,
                        PurchaseType.NON_SUBSCRIPTION,
                        status,
                        UUID.fromString(acId),
                        null,
                        now,
                        now
                )
        );
    }

    private Uni<Purchase> persistSubscription(
            String accountId,
            ProductData productData,
            String token,
            SubscriptionPurchase purchase
    ) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime purchaseDate = toOffsetDateTime(purchase.getStartTimeMillis(), now);
        OffsetDateTime expiryDate = toOffsetDateTime(purchase.getExpiryTimeMillis(), null);
        String acId = accountId != null ? accountId : purchase.getObfuscatedExternalAccountId();

        return repository.createOrUpdate(
                new Purchase(
                        null,
                        IapSource.GOOGLE_PLAY,
                        resolveOrderId(
                                purchase.getOrderId(),
                                accountId,
                                productData.externalProductId(),
                                token,
                                PurchaseType.SUBSCRIPTION
                        ),
                        productData.externalProductId(),
                        purchaseDate,
                        expiryDate,
                        PurchaseType.SUBSCRIPTION,
                        mapSubscriptionStatus(purchase, expiryDate),
                        UUID.fromString(acId),
                        null,
                        now,
                        now
                )
        );
    }

    private PurchaseStatus mapSubscriptionStatus(
            SubscriptionPurchase purchase,
            OffsetDateTime expiryDate
    ) {
        if (expiryDate != null && !expiryDate.isAfter(OffsetDateTime.now(ZoneOffset.UTC))) {
            return PurchaseStatus.EXPIRED;
        }

        Integer paymentState = purchase.getPaymentState();
        return switch (paymentState == null ? -1 : paymentState) {
            case 1, 2 -> PurchaseStatus.ACTIVE;
            case 0, 3 -> PurchaseStatus.PENDING;
            default -> PurchaseStatus.PENDING;
        };
    }

    private OffsetDateTime toOffsetDateTime(Long epochMillis, OffsetDateTime fallback) {
        if (epochMillis == null || epochMillis <= 0) {
            return fallback;
        }
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC);
    }

    private String resolveOrderId(
            String orderId,
            String accountId,
            String externalProductId,
            String token,
            PurchaseType purchaseType
    ) {
        if (orderId != null && !orderId.isBlank()) {
            return orderId;
        }
        return "%s:%s:%s:%s".formatted(purchaseType.getValue(), accountId, externalProductId, token);
    }

    private void validateRequest(String accountId, ProductData productData, String token) {
        if (productData == null) {
            throw new IllegalArgumentException("productData must not be null");
        }
        if (productData.platform() != ProductPlatform.ANDROID) {
            throw new IllegalArgumentException("GooglePlayPurchaseHandler supports only Android products");
        }
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("accountId must not be blank");
        }
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
        if (productData.packageName() == null || productData.packageName().isBlank()) {
            throw new IllegalArgumentException("packageName must not be blank");
        }
        if (productData.externalProductId() == null || productData.externalProductId().isBlank()) {
            throw new IllegalArgumentException("externalProductId must not be blank");
        }
    }
}
