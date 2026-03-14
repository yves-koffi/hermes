package account.application.result;

import java.time.OffsetDateTime;

public record PasswordResetDetails(
        OffsetDateTime resetAt
) {
}
