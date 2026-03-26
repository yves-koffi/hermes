package account.infrastructure.messaging;

import account.application.event.EmailDispatchEvent;
import account.application.spi.NotificationByEmailDispatchPublisher;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EventBusEmailDispatchPublisher implements NotificationByEmailDispatchPublisher {

    @Inject
    EventBus eventBus;

    @Override
    public Uni<Void> publish(EmailDispatchEvent event) {
        eventBus.publish(EmailDispatchEvent.ADDRESS, new JsonObject()
                .put("recipient", event.recipient())
                .put("tokenType", event.tokenType().name())
                .put("language", event.language())
                .put("tokenValue", event.tokenValue()));
        return Uni.createFrom().voidItem();
    }
}
