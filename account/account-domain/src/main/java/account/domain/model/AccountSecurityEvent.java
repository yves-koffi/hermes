package account.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AccountSecurityEvent(
        UUID id,
        UUID accountId,
        AccountSecurityEventType eventType,
        String detail,
        String ipAddress,
        OffsetDateTime occurredAt
) {
}
