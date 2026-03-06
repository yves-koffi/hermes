package store.purchase.infrastructure.store.app.store;

import com.apple.itunes.storekit.model.*;
import com.apple.itunes.storekit.verification.SignedDataVerifier;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import store.purchase.infrastructure.persistence.entity.ProductEntity;
import store.purchase.infrastructure.persistence.repository.ProductEntityRepository;


@ApplicationScoped
public class AppleNotificationHandler {

    @Inject
    ProductEntityRepository repository;

    @Inject
    AppleVerifierService verifierService;

    public Uni<Void> processNotification(String signedPayload, Long appleId) throws Exception {

        return repository.findByAppleId(appleId).flatMap(product -> {
            if (product.isPresent()) {
                try {

                    SignedDataVerifier verifier =
                            verifierService.getVerifier(
                                    product.get().getBundleId(),
                                    product.get().getAppAppleId()
                            );

                    ResponseBodyV2DecodedPayload decoded =
                            verifier.verifyAndDecodeNotification(signedPayload);

                    handleNotification(decoded, product.get());

                } catch (Exception ignored) {
                    return Uni.createFrom().failure(new RuntimeException("No valid bundleId found for notification"));
                }
            }
            return Uni.createFrom().voidItem();
        });

    }

    private void handleNotification(
            ResponseBodyV2DecodedPayload notification,
            ProductEntity product
    ) {

        NotificationTypeV2 type = notification.getNotificationType();
        Subtype subtype = notification.getSubtype();

        Data data = notification.getData();

        if (data == null) return;

        String signedTx = data.getSignedTransactionInfo();

        if (signedTx == null) return;

        SignedDataVerifier verifier =
                verifierService.getVerifier(product.getBundleId(), product.getAppAppleId());

        try {

            JWSTransactionDecodedPayload transaction =
                    verifier.verifyAndDecodeTransaction(signedTx);


            String originalTransactionId =
                    transaction.getOriginalTransactionId();

            Long expiresDate = transaction.getExpiresDate();

            // update subscription ici

        } catch (Exception e) {

            throw new RuntimeException(e);

        }
    }
}