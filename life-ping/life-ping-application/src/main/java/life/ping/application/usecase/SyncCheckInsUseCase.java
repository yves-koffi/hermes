package life.ping.application.usecase;

import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import life.ping.domain.model.CheckinSource;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Sync check-ins offline (idempotent).
 */
public interface SyncCheckInsUseCase {

    Uni<Output> handle(@Valid @NotNull Input in);

    record Input(
            @NotNull UUID userId,
            @NotNull @Size(min = 1) List<@Valid @NotNull Item> items
    ) {}

    record Item(
            @NotNull LocalDate localDate,
            Instant checkedInAt,     // optional
            CheckinSource source     // SYNC
    ) {}

    record Output(
            int received,
            int inserted,
            int skippedDuplicates,
            Instant serverTime
    ) {}
}
