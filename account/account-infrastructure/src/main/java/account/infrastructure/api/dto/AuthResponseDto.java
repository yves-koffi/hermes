package account.infrastructure.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuthResponseDto(
        UUID accountId,
        boolean verified,
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessExpiresIn,
        long refreshExpiresIn,
        OffsetDateTime accessExpiresAt,
        OffsetDateTime refreshExpiresAt
) {
}
