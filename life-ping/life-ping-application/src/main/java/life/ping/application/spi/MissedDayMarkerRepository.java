package life.ping.application.spi;

import io.smallrye.mutiny.Uni;
import life.ping.domain.model.MissedDayMarker;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MissedDayMarkerRepository {
    Uni<MissedDayMarker> save(MissedDayMarker missedDayMarker);

    Uni<Optional<MissedDayMarker>> findById(UUID id);

    Uni<List<MissedDayMarker>> findAll();

    Uni<Void> deleteById(UUID id);
}
