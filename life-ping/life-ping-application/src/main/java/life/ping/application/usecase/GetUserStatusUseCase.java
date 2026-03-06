package life.ping.application.usecase;

import io.smallrye.mutiny.Uni;
import life.ping.domain.model.DayStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-06: Récupérer le statut courant (UI).
 */
public interface GetUserStatusUseCase {

    Uni<Output> handle(Input in);

    record Input(String appUid) {}

    record Output(
            DayStatus todayStatus,
            int missedStreak,
            int maxMissedDays,
            Instant lastCheckinAt,
            String emergencyContactEmail,
            Instant lastAlertAt
    ) {}
}