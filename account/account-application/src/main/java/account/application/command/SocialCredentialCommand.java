package account.application.command;

import account.domain.model.Provider;

public record SocialCredentialCommand(
        Provider provider,
        String providerId,
        String displayName,
        String email,
        String photoUrl
) {
}
