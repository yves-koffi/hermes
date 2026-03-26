package store.purchase.infrastructure.listener;

import com.google.api.services.androidpublisher.AndroidPublisher;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import store.purchase.application.spi.PurchaseHandler;
import store.purchase.domain.ProductPlatform;
import store.purchase.infrastructure.api.dto.ReceivePubsubMessageRequest;
import store.purchase.infrastructure.event.GooglePlayPurchaseEvent;

import java.util.Optional;

@ApplicationScoped
public class GooglePlayPurchaseEventListener {

    @Inject
    Instance<PurchaseHandler<AndroidPublisher, ReceivePubsubMessageRequest>> handlers;
    private static final Logger LOGGER = LoggerFactory.getLogger(GooglePlayPurchaseEventListener.class);

    public Uni<Void> onGooglePlayPurchaseEvent(
            @ObservesAsync GooglePlayPurchaseEvent event
    ) {
        if (event == null) {
            LOGGER.warn("GooglePlayPurchaseEvent reçu avec une valeur nulle.");
            return Uni.createFrom().voidItem();
        }
        Optional<PurchaseHandler<AndroidPublisher, ReceivePubsubMessageRequest>> handler = handlers.stream()
                .filter(it -> it.supports(ProductPlatform.IOS))
                .findFirst();

        LOGGER.info(
                "GooglePlayPurchaseEvent reçu: messageId='{}', subscription='{}'.",
                event.messageId(),
                event.subscription()
        );

        if (handler.isPresent()) {
            return handler.get().handlePullPurchase(
                    new ReceivePubsubMessageRequest(
                            event.messageId(),
                            event.subscription(),
                            event.notificationData()
                    )
            ).replaceWithVoid();
        }

        return Uni.createFrom().voidItem();
    }
}
