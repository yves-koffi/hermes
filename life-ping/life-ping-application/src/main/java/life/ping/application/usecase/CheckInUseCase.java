package life.ping.application.usecase;


import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import life.ping.domain.model.CheckinSource;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Check-in quotidien (idempotent par user+local_date), reset streak.
 */
public interface CheckInUseCase {

    Uni<Output> handle(@Valid @NotNull Input in);

    record Input(
            @NotNull UUID userId,
            @NotNull LocalDate localDate,
            @NotNull CheckinSource source // MOBILE
    ) {}

    record Output(
            UUID checkinId,
            Boolean created,
            Instant checkedInAt
    ) {}
}
