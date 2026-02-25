package life.ping.application.usecase;

import life.ping.domain.model.DayStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * UC-06: Récupérer le statut courant (UI).
 */
public interface GetUserStatusUseCase {

    Output handle(Input in);

    record Input(UUID userId) {}

    record Output(
            DayStatus todayStatus,
            int missedStreak,
            int maxMissedDays,
            Instant lastCheckinAt,
            String emergencyContactEmail,
            Instant lastAlertAt
    ) {}
}