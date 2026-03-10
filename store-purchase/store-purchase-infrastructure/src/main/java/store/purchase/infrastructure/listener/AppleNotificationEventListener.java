package store.purchase.infrastructure.listener;

import com.apple.itunes.storekit.verification.SignedDataVerifier;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import store.purchase.application.spi.PurchaseHandler;
import store.purchase.domain.ProductPlatform;
import store.purchase.infrastructure.api.dto.ReceiveAppleNotificationRequest;
import store.purchase.infrastructure.event.AppleNotificationEvent;

import java.util.Optional;

@ApplicationScoped
public class AppleNotificationEventListener {
    @Inject
    Instance<PurchaseHandler<SignedDataVerifier, ReceiveAppleNotificationRequest>> handlers;
    private static final Logger LOGGER = LoggerFactory.getLogger(AppleNotificationEventListener.class);

    public Uni<Void> onAppleNotificationEvent(@ObservesAsync AppleNotificationEvent event) {
        if (event == null) {
            LOGGER.warn("AppleNotificationEvent recu avec une valeur nulle.");
            return Uni.createFrom().voidItem();
        }
        Optional<PurchaseHandler<SignedDataVerifier, ReceiveAppleNotificationRequest>> handler = handlers.stream()
                .filter(it -> it.supports(ProductPlatform.IOS))
                .findFirst();

        if (handler.isPresent()) {
            return handler.get().handlePullPurchase(
                    new ReceiveAppleNotificationRequest(
                          event.payload(),
                          event.product()
                    )
            ).replaceWithVoid();
        }

        LOGGER.info(
                "AppleNotificationEvent recu: productId='{}', notificationType='{}'.",
                event.product() != null ? event.product().getId() : null,
                event.payload() != null ? event.payload().getNotificationType() : null
        );
        return Uni.createFrom().voidItem();
    }
}
