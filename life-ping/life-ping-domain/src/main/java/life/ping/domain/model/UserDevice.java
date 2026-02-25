package life.ping.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDevice(
        UUID id,
        UUID userId,
        String platform,
        String fcmToken,
        LocalDateTime lastSeenAt
) {
}
