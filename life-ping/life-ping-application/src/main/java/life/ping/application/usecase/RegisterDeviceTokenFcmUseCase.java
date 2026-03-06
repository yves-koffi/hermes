package life.ping.application.usecase;

import io.smallrye.mutiny.Uni;

import java.time.Instant;
import java.util.UUID;

/**
 * Enregistrer / upsert le token FCM.
 */
public interface RegisterDeviceTokenFcmUseCase {
    Uni<Void> handle(Input in);

    record Input(
            UUID appUid,
            String platform,      // ANDROID/IOS
            String fcmToken,
            Instant seenAt
    ) {}

    record Output(
            String platform,      // ANDROID/IOS
            String fcmToken
    ) {}
}
