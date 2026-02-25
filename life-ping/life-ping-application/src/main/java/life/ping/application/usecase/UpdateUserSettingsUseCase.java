package life.ping.application.usecase;

import java.time.LocalTime;
import java.util.UUID;

/**
 * Mettre à jour les paramètres utilisateur (timezone / reminder / maxMissedDays) and contact d'urgence.
 */
public interface UpdateUserSettingsUseCase {
    void handle(Input in);

    record Input(
            UUID userId,
            String timezone,
            LocalTime dailyReminderTime,
            int maxMissedDays
    ) {}
}
