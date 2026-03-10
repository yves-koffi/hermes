package account.application.result;

import account.domain.model.Provider;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AccountSummary(
        UUID id,
        String name,
        Provider provider,
        OffsetDateTime activatedAt
) {
}