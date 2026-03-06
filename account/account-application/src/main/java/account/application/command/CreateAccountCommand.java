package account.application.command;

import account.domain.model.Provider;

public record CreateAccountCommand(
        String name,
        String email,
        String password,
        String prefix,
        String number,
        String avatarUrl,
        String providerId,
        Provider provider
) {
}
