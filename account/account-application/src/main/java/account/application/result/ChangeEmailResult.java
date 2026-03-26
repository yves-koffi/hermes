package account.application.result;

import java.util.UUID;

public record ChangeEmailResult(
        UUID accountId,
        String email,
        boolean verificationRequired,
        String nextStep
) {
}
