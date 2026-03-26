package account.application.result;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PasswordResetResult(
        UUID accountId,
        boolean passwordUpdated,
        boolean sessionsRevoked,
        OffsetDateTime resetAt
) {
}
