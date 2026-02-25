package life.ping.application.spi;

import io.smallrye.mutiny.Uni;
import life.ping.domain.model.DailyCheckin;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DailyCheckinRepository {
    Uni<DailyCheckin> save(DailyCheckin dailyCheckin);

    Uni<Optional<DailyCheckin>> findById(UUID id);

    Uni<List<DailyCheckin>> findAll();

    Uni<Void> deleteById(UUID id);
}
