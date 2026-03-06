package life.ping.application.usecase;

import io.smallrye.mutiny.Uni;

import java.time.LocalTime;
import java.util.UUID;

/**
 * Mettre à jour les paramètres utilisateur (timezone / reminder / maxMissedDays) and contact d'urgence.
 */
public interface UpdateUserSettingsUseCase {
    Uni<Void> handle(Input in);

    record Input(
            String appUid,
            String name,
            LocalTime callbackTime,
            Integer checkInFrequency,
            Integer thresholdPeriod,

            String emergencyContactName,
            String emergencyContactEmail,
            String notificationLanguage
    ) {
    }
}