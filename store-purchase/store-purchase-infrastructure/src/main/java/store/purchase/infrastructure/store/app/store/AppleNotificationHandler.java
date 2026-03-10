package store.purchase.infrastructure.store.app.store;

import com.apple.itunes.storekit.model.JWSRenewalInfoDecodedPayload;
import com.apple.itunes.storekit.model.JWSTransactionDecodedPayload;
import com.apple.itunes.storekit.model.ResponseBodyV2DecodedPayload;
import com.apple.itunes.storekit.verification.SignedDataVerifier;
import com.apple.itunes.storekit.verification.VerificationException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import store.purchase.infrastructure.event.AppleNotificationEvent;
import store.purchase.infrastructure.persistence.repository.ProductEntityRepository;

import java.util.Optional;


@ApplicationScoped
public class AppleNotificationHandler {

    @Inject
    ProductEntityRepository repository;

    @Inject
    AppleVerifierService verifierService;
    @Inject
    Event<AppleNotificationEvent> appleNotificationEvent;

    public Uni<Void> processNotification(
            String signedPayload,
            Long appleId
    ) {

        return repository.findByAppleId(appleId).flatMap(product -> {
            if (product.isPresent()) {
                try {

                    SignedDataVerifier verifier =
                            verifierService.getVerifier(
                                    product.get().getBundleId(),
                                    product.get().getAppAppleId()
                            );

                    ResponseBodyV2DecodedPayload payload =
                            verifier.verifyAndDecodeNotification(signedPayload);

                    appleNotificationEvent.fireAsync(new AppleNotificationEvent(payload, product.get()));

                } catch (Exception ignored) {
                    return Uni.createFrom().failure(new RuntimeException("No valid bundleId found for payload"));
                }
            }
            return Uni.createFrom().voidItem();
        });

    }


}
