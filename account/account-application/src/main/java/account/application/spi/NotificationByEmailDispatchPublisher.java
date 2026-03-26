package account.application.spi;

import account.application.event.EmailDispatchEvent;
import io.smallrye.mutiny.Uni;

public interface NotificationByEmailDispatchPublisher {

    Uni<Void> publish(EmailDispatchEvent event);
}
