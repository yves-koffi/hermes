package store.purchase.infrastructure.listener;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import store.purchase.infrastructure.event.GooglePlayPurchaseEvent;

@ApplicationScoped
public class GooglePlayPurchaseEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(GooglePlayPurchaseEventListener.class);

    public Uni<Void> onGooglePlayPurchaseEvent(@ObservesAsync GooglePlayPurchaseEvent event) {
        if (event == null) {
            LOGGER.warn("GooglePlayPurchaseEvent recu avec une valeur nulle.");

        }

        LOGGER.info(
                "GooglePlayPurchaseEvent recu: messageId='{}', subscription='{}'.",
                event.messageId(),
                event.subscription()
        );

        return  Uni.createFrom().voidItem();
    }
}
