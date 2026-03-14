package account.application.result;

import java.time.OffsetDateTime;

public record AccountVerificationDetails(
        OffsetDateTime verifiedAt
) {
}
