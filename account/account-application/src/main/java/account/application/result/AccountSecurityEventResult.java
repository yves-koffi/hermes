package account.application.result;

import account.domain.model.AccountSecurityEventType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AccountSecurityEventResult(
        UUID id,
        AccountSecurityEventType eventType,
        String detail,
        String ipAddress,
        OffsetDateTime occurredAt
) {
}
