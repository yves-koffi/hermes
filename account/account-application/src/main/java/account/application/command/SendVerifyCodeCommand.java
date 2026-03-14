package account.application.command;

import account.domain.model.TokenType;

public record SendVerifyCodeCommand(
        String email,
        TokenType type
) {
}
