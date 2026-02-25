package life.ping.application.usecase;

import java.time.Instant;
import java.util.UUID;

/**
 * Enregistrer / upsert le token FCM.
 */
public interface RegisterDeviceTokenFcmUseCase {
    void handle(Input in);

    record Input(
            UUID userId,
            String platform,      // ANDROID/IOS
            String fcmToken,
            Instant seenAt
    ) {}
}
