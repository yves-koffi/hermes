package account.application.command;

import account.domain.model.TokenType;

public record ForgetPasswordCommand(
        String email,
        TokenType type
) {
}
