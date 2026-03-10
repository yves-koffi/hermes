package store.purchase.infrastructure.store.app.store;

import com.apple.itunes.storekit.client.APIException;
import com.apple.itunes.storekit.client.AppStoreServerAPIClient;
import com.apple.itunes.storekit.migration.ReceiptUtility;
import com.apple.itunes.storekit.model.JWSTransactionDecodedPayload;
import com.apple.itunes.storekit.model.Type;
import com.apple.itunes.storekit.verification.SignedDataVerifier;
import com.apple.itunes.storekit.verification.VerificationException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import store.purchase.application.spi.PurchaseHandler;
import store.purchase.application.spi.PurchaseRepository;
import store.purchase.domain.IapSource;
import store.purchase.domain.ProductData;
import store.purchase.domain.ProductPlatform;
import store.purchase.domain.ProductType;
import store.purchase.domain.Purchase;
import store.purchase.domain.PurchaseStatus;
import store.purchase.domain.PurchaseType;
import store.purchase.infrastructure.api.dto.ReceiveAppleNotificationRequest;
import store.purchase.infrastructure.config.AppStoreVerifierConfig;
import store.purchase.infrastructure.config.CertificateProducer;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@ApplicationScoped
public class AppStorePurchaseHandler implements PurchaseHandler<SignedDataVerifier, ReceiveAppleNotificationRequest> {

    private final ReceiptUtility receiptUtility = new ReceiptUtility();
    private final AppStoreVerifierConfig appStoreVerifierConfig;
    private volatile SignedDataVerifier client;

    @Inject
    PurchaseRepository repository;
    @Inject
    CertificateProducer certificateProducer;

    @Inject
    public AppStorePurchaseHandler(AppStoreVerifierConfig appStoreVerifierConfig) {
        this.appStoreVerifierConfig = appStoreVerifierConfig;
    }

    public AppStorePurchaseHandler(
            String bundleId,
            Long appAppleId,
            AppStoreVerifierConfig appStoreVerifierConfig
    ) {
        this.appStoreVerifierConfig = appStoreVerifierConfig;
        this.client = buildClient(bundleId, appAppleId);
    }

    @Override
    public boolean supports(ProductPlatform platform) {
        return platform == ProductPlatform.IOS;
    }

    @Override
    public Uni<Boolean> verifyPurchase(
            String accountId,
            ProductData productData,
            String token
    ) {
        validateRequest(accountId, productData, token);

        if (productData.type() == ProductType.Subscription) {
            return handleSubscription(accountId, productData, token)
                    .map(Objects::nonNull);
        }

        return handleNonSubscription(accountId, productData, token)
                .map(Objects::nonNull);
    }

    @Override
    public Uni<Purchase> handleNonSubscription(
            String accountId,
            ProductData productData,
            String token
    ) {
        validateRequest(accountId, productData, token);

        return Uni.createFrom()
                .item(() -> verifyTransaction(accountId, productData, token))
                .chain(transaction -> persistNonSubscription(accountId, productData, token, transaction));
    }

    @Override
    public Uni<Purchase> handleSubscription(
            String accountId,
            ProductData productData,
            String token
    ) {
        validateRequest(accountId, productData, token);

        return Uni.createFrom()
                .item(() -> verifyTransaction(accountId, productData, token))
                .chain(transaction -> persistSubscription(accountId, productData, token, transaction));
    }


    @Override
    public Uni<Void> handlePullPurchase(ReceiveAppleNotificationRequest request) {


        return Uni.createFrom().voidItem();
    }

    @Override
    public SignedDataVerifier getClient() {
        if (client == null) {
            throw new IllegalStateException(
                    "SignedDataVerifier is not initialized. Verify a purchase first or use the explicit constructor."
            );
        }
        return client;
    }

    private JWSTransactionDecodedPayload verifyTransaction(
            String accountId,
            ProductData productData,
            String token
    ) {
        SignedDataVerifier verifier = configureClient(productData);
        String signedTransaction = resolveSignedTransactionToken(productData, token);

        try {

            JWSTransactionDecodedPayload transaction = verifier.verifyAndDecodeTransaction(signedTransaction);
            validateTransaction(accountId, productData, transaction);
            return transaction;
        } catch (VerificationException exception) {
            throw new RuntimeException("Apple transaction verification failed: " + exception.getStatus(), exception);
        }
    }

    private Uni<Purchase> persistNonSubscription(
            String accountId,
            ProductData productData,
            String token,
            JWSTransactionDecodedPayload transaction
    ) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        return repository.createOrUpdate(
                new Purchase(
                        null,
                        IapSource.APP_STORE,
                        resolveOrderId(transaction, accountId, productData.externalProductId(), token, PurchaseType.NON_SUBSCRIPTION),
                        productData.externalProductId(),
                        resolvePurchaseDate(transaction, now),
                        null,
                        PurchaseType.NON_SUBSCRIPTION,
                        transaction.getRevocationDate() == null ? PurchaseStatus.COMPLETED : PurchaseStatus.CANCELLED,
                        UUID.fromString(accountId),
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
            JWSTransactionDecodedPayload transaction
    ) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expiryDate = toOffsetDateTime(transaction.getExpiresDate(), null);

        return repository.createOrUpdate(
                new Purchase(
                        null,
                        IapSource.APP_STORE,
                        resolveOrderId(transaction, accountId, productData.externalProductId(), token, PurchaseType.SUBSCRIPTION),
                        productData.externalProductId(),
                        resolvePurchaseDate(transaction, now),
                        expiryDate,
                        PurchaseType.SUBSCRIPTION,
                        mapSubscriptionStatus(transaction, expiryDate),
                        UUID.fromString(accountId),
                        null,
                        now,
                        now
                )
        );
    }

    private PurchaseStatus mapSubscriptionStatus(
            JWSTransactionDecodedPayload transaction,
            OffsetDateTime expiryDate
    ) {
        if (transaction.getRevocationDate() != null) {
            return PurchaseStatus.EXPIRED;
        }
        if (Boolean.TRUE.equals(transaction.getIsUpgraded())) {
            return PurchaseStatus.EXPIRED;
        }
        if (expiryDate != null && !expiryDate.isAfter(OffsetDateTime.now(ZoneOffset.UTC))) {
            return PurchaseStatus.EXPIRED;
        }
        if (expiryDate == null) {
            return PurchaseStatus.PENDING;
        }
        return PurchaseStatus.ACTIVE;
    }

    private SignedDataVerifier configureClient(ProductData productData) {
        if (productData == null) {
            throw new IllegalStateException("productData must not be null");
        }

        SignedDataVerifier verifier = buildClient(productData.bundleId(), productData.appAppleId());
        this.client = verifier;
        return verifier;
    }

    private SignedDataVerifier buildClient(String bundleId, Long appAppleId) {
        validateConfiguration(bundleId, appAppleId);

        return new SignedDataVerifier(
                certificateProducer.produceLoadedCertificates(),
                bundleId,
                appAppleId,
                appStoreVerifierConfig.parsedEnvironment(),
                appStoreVerifierConfig.enableOnlineChecks()
        );
    }

    private String resolveSignedTransactionToken(ProductData productData, String token) {
        if (!appStoreVerifierConfig.enableApiLookup()) {
            return token;
        }

        String transactionId = extractTransactionIdFromReceipt(token);
        if (transactionId == null || transactionId.isBlank()) {
            return token;
        }

        if (appStoreVerifierConfig.issuerId().isBlank()
                || appStoreVerifierConfig.keyId().isBlank()
                || appStoreVerifierConfig.privateKeyPath().isBlank()) {
            return token;
        }

        try {
            String signedTransactionInfo = buildApiClient(productData)
                    .getTransactionInfo(transactionId)
                    .getSignedTransactionInfo();
            if (signedTransactionInfo == null || signedTransactionInfo.isBlank()) {
                return token;
            }
            return signedTransactionInfo;
        } catch (IOException | APIException exception) {
            throw new RuntimeException("Unable to fetch Apple transaction info", exception);
        }
    }

    private String extractTransactionIdFromReceipt(String token) {
        try {
            return receiptUtility.extractTransactionIdFromAppReceipt(token);
        } catch (IOException exception) {
            return null;
        }
    }

    private AppStoreServerAPIClient buildApiClient(ProductData productData) {
        return new AppStoreServerAPIClient(
                loadRequiredResourceAsString(appStoreVerifierConfig.privateKeyPath()),
                appStoreVerifierConfig.keyId(),
                appStoreVerifierConfig.issuerId(),
                productData.bundleId(),
                appStoreVerifierConfig.parsedEnvironment()
        );
    }

    private void validateRequest(String accountId, ProductData productData, String token) {
        if (productData == null) {
            throw new IllegalArgumentException("productData must not be null");
        }
        if (productData.platform() != ProductPlatform.IOS) {
            throw new IllegalArgumentException("AppStorePurchaseHandler supports only iOS products");
        }
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("accountId must not be blank");
        }
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
        if (productData.externalProductId() == null || productData.externalProductId().isBlank()) {
            throw new IllegalArgumentException("externalProductId must not be blank");
        }
        validateConfiguration(productData.bundleId(), productData.appAppleId());

    }

    private void validateTransaction(
            String accountId,
            ProductData productData,
            JWSTransactionDecodedPayload transaction
    ) {
        if (transaction == null) {
            throw new IllegalArgumentException("Decoded Apple transaction must not be null");
        }
        if (!Objects.equals(productData.externalProductId(), transaction.getProductId())) {
            throw new IllegalArgumentException("Apple transaction productId does not match requested product");
        }
        if (!Objects.equals(productData.bundleId(), transaction.getBundleId())) {
            throw new IllegalArgumentException("Apple transaction bundleId does not match configured bundle");
        }
        if (transaction.getAppAccountToken() != null
                && !Objects.equals(transaction.getAppAccountToken(), UUID.fromString(accountId))) {
            throw new IllegalArgumentException("Apple transaction appAccountToken does not match accountId");
        }

        validateAppleType(productData.type(), transaction.getType());
    }

    private void validateAppleType(ProductType productType, Type appleType) {
        if (appleType == null) {
            return;
        }

        if (productType == ProductType.Subscription) {
            if (appleType != Type.AUTO_RENEWABLE_SUBSCRIPTION
                    && appleType != Type.NON_RENEWING_SUBSCRIPTION) {
                throw new IllegalArgumentException("Apple transaction type does not match subscription product");
            }
            return;
        }

        if (appleType != Type.CONSUMABLE && appleType != Type.NON_CONSUMABLE) {
            throw new IllegalArgumentException("Apple transaction type does not match non-subscription product");
        }
    }

    private void validateConfiguration(String bundleId, Long appAppleId) {
        if (bundleId == null || bundleId.isBlank() || appAppleId == null || appAppleId <= 0) {
            throw new IllegalStateException("bundleId and appAppleId must be provided");
        }
    }


    private String loadRequiredResourceAsString(String resourcePath) {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IllegalStateException("Resource not found: " + resourcePath);
        }

        try (inputStream) {
            return new String(inputStream.readAllBytes());
        } catch (IOException exception) {
            throw new RuntimeException("Unable to read resource: " + resourcePath, exception);
        }
    }

    private String resolveOrderId(
            JWSTransactionDecodedPayload transaction,
            String accountId,
            String externalProductId,
            String token,
            PurchaseType purchaseType
    ) {
        if (transaction.getOriginalTransactionId() != null && !transaction.getOriginalTransactionId().isBlank()) {
            return transaction.getOriginalTransactionId();
        }
        if (transaction.getTransactionId() != null && !transaction.getTransactionId().isBlank()) {
            return transaction.getTransactionId();
        }
        if (transaction.getWebOrderLineItemId() != null && !transaction.getWebOrderLineItemId().isBlank()) {
            return transaction.getWebOrderLineItemId();
        }
        return "%s:%s:%s:%s".formatted(purchaseType.getValue(), accountId, externalProductId, token);
    }

    private OffsetDateTime resolvePurchaseDate(
            JWSTransactionDecodedPayload transaction,
            OffsetDateTime fallback
    ) {
        OffsetDateTime originalPurchaseDate = toOffsetDateTime(transaction.getOriginalPurchaseDate(), null);
        if (originalPurchaseDate != null) {
            return originalPurchaseDate;
        }
        return toOffsetDateTime(transaction.getPurchaseDate(), fallback);
    }

    private OffsetDateTime toOffsetDateTime(Long epochMillis, OffsetDateTime fallback) {
        if (epochMillis == null || epochMillis <= 0) {
            return fallback;
        }
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC);
    }


}
