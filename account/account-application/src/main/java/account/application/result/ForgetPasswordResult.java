package account.application.result;

import account.domain.model.TokenType;

import java.time.OffsetDateTime;

public record ForgetPasswordResult(
        boolean accepted,
        String deliveryChannel,
        TokenType tokenType,
        OffsetDateTime expiresAt
) {
}
