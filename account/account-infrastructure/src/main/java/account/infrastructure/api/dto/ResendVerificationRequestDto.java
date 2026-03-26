package account.infrastructure.api.dto;

import account.domain.model.TokenType;

public record ResendVerificationRequestDto(
        String email,
        TokenType type
) {
}
