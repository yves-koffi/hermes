package business.card.domain.model;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public record Account(
        UUID id,
        String name,
        String email,
        String avatarUrl,
        String googleId,
        String appleId,
        OffsetDateTime activatedAt,
        Provider provider, // Spécifie explicitement que Provider est une enum imbriquée dans Account
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public boolean isActivated() {
        return activatedAt != null;
    }

}