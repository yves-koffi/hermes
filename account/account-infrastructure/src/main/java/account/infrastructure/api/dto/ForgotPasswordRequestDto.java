package account.infrastructure.api.dto;

import account.domain.model.TokenType;

public record ForgotPasswordRequestDto(
        String email,
        TokenType type
) {
}
