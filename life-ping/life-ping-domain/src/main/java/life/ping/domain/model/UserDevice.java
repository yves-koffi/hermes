package life.ping.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDevice(
        UUID id,
        UUID accountId,
        String platform,
        String fcmToken,
        LocalDateTime createdAt,
        LocalDateTime lastSeenAt,
        LocalDateTime softDeletedAt
) {
}
