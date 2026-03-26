package account.application.result;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AccountVerificationResult(
        UUID accountId,
        boolean verified,
        OffsetDateTime verifiedAt
) {
}
