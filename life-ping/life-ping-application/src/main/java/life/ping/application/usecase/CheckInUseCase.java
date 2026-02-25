package life.ping.application.usecase;


import life.ping.domain.model.CheckinSource;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Check-in quotidien (idempotent par user+local_date), reset streak.
 */
public interface CheckInUseCase {

    Output handle(Input in);

    record Input(
            UUID userId,
            LocalDate localDate,
            CheckinSource source // MOBILE
    ) {}

    record Output(
            UUID checkinId,
            boolean created,
            Instant checkedInAt
    ) {}
}
