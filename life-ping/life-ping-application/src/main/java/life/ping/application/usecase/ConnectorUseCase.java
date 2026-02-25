package life.ping.application.usecase;

import java.util.UUID;

public interface ConnectorUseCase {
    Output handle(Input in);

    record Input(
            String deviceUniqueId,
            String appUuid,
            String deviceModel,
            String devicePlatform,     // ANDROID / IOS
            String timezone
    ) {
    }

    record Output(
            UUID userId,
            String accessToken,
            String timezone,
            String token
    ) {
    }
}
