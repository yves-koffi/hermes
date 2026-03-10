package account.application.result;

import account.domain.model.PhoneNumber;
import account.domain.model.Provider;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AccountDetails(
        UUID id,
        String name,
        String email,
        PhoneNumber phoneNumber,
        String avatarUrl,
        Provider provider,
        OffsetDateTime activatedAt,
        OffsetDateTime createdAt
) {
}