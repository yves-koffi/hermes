package account.application.result;

import java.time.OffsetDateTime;

public record PasswordResetResult(
        OffsetDateTime resetAt
) {
}
