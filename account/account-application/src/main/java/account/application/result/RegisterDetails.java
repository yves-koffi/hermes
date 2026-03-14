package account.application.result;

import java.util.UUID;

public record RegisterDetails(
        UUID accountId,
        boolean verificationRequired
) {
}
