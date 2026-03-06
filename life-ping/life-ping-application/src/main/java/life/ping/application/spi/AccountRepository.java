package life.ping.application.spi;

import io.smallrye.mutiny.Uni;
import life.ping.domain.model.Account;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    Uni<Account> save(Account account);

    Uni<Optional<Account>> findById(UUID id);

    Uni<Optional<Account>> findByAppUuid(String appUuid);

    Uni<Integer> updateCheckinState(UUID id, LocalDateTime lastCheckinAt, LocalDateTime updatedAt, boolean resetMissedStreak);

    Uni<Integer> updateUserSettings(UUID id, String userName, java.time.LocalTime callbackTime, Integer checkInFrequency, Integer thresholdPeriod, LocalDateTime updatedAt);

    Uni<List<Account>> findAll();

    Uni<Void> deleteById(UUID id);
}
