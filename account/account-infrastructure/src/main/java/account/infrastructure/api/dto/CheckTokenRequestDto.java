package account.infrastructure.api.dto;

import account.domain.model.TokenType;

public record CheckTokenRequestDto(
        String token,
        TokenType type
) {
}
