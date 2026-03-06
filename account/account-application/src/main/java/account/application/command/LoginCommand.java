package account.application.command;


import account.domain.model.Provider;

public record LoginCommand(
        Provider provider,
        String email,
        String password
) {
}