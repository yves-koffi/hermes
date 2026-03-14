package account.application.command;

import account.domain.model.TokenType;

public record CheckTokenCommand(
        String token,
        TokenType type
) {
}
