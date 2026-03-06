package account.application.result;

import account.domain.model.Provider;

public record AccountSummary(
        String id,
        String name,
        String email,
        Provider provider,
        Boolean activated
) {
}