package account.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Account(
        UUID id,
        String name,
        String email,
        PhoneNumber phoneNumber,//basic provider
        String password,//basic provider
        String avatarUrl,
        String providerId,
        Provider provider, // Spécifie explicitement que Provider est une enum imbriquée dans Account
        OffsetDateTime activatedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public boolean isActivated() {
        return activatedAt != null;
    }

}
