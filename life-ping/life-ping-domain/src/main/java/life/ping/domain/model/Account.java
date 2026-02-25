package life.ping.domain.model;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record Account(
        UUID id,
        String userName,
        String appUuid,
        String deviceUniqueId,
        String deviceModel,
        String devicePlatform,
        String timezone,
        LocalTime callbackTime,
        Integer checkInFrequency,
        Integer thresholdPeriod,
        LocalDateTime lastCheckinAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
