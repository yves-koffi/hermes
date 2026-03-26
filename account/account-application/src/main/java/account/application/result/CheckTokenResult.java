package account.application.result;

import java.time.OffsetDateTime;

public record CheckTokenResult(
        boolean valid,
        String tokenType,
        OffsetDateTime expiresAt
) {
}
