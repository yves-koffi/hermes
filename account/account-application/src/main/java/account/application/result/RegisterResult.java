package account.application.result;

import java.util.UUID;

public record RegisterResult(
        UUID accountId,
        boolean verificationRequired
) {
}
