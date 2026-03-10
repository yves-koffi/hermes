package account.application.result;

import java.time.OffsetDateTime;

public record AccountVerificationResult(
        OffsetDateTime verifiedAt
) {
}
