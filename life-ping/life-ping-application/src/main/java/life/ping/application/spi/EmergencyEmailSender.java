package life.ping.application.spi;

import io.smallrye.mutiny.Uni;
import life.ping.domain.model.Account;
import life.ping.domain.model.EmergencyContact;

import java.time.Instant;
import java.util.List;

public interface EmergencyEmailSender {
    Uni<Void> sendSosAlert(Account account, List<EmergencyContact> contacts, Instant requestedAt);
}
