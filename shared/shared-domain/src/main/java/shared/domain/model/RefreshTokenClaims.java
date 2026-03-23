package shared.domain.model;

import java.util.UUID;

public record RefreshTokenClaims(
        UUID sessionId,
        UUID accountId,
        String tokenId
) {
}
