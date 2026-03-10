package account.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record HashToken(
        UUID id,
        String hashToken,
        String token,
        TokenType tokenType,
        UUID accountId,
        OffsetDateTime expiryDate,
        String ipAddress,
        OffsetDateTime revokedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
