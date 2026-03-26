package account.infrastructure.api.dto;

import account.domain.model.TokenType;

public record VerifyEmailRequestDto(
        String token,
        TokenType type
) {
}
