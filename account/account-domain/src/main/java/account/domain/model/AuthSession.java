package account.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuthSession(
        UUID id,
        UUID accountId,
        String refreshTokenHash,
        OffsetDateTime expiryDate,
        String ipAddress,
        String userAgent,
        UUID rotatedFromSessionId,
        OffsetDateTime lastUsedAt,
        OffsetDateTime revokedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
