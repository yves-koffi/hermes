package life.ping.application.usecase;

import life.ping.domain.model.CheckinSource;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Sync check-ins offline (idempotent).
 */
public interface SyncCheckInsUseCase {

    Output handle(Input in);

    record Input(
            UUID userId,
            List<Item> items
    ) {}

    record Item(
            LocalDate localDate,
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
