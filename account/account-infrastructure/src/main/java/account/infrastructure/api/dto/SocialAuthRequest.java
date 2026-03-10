package account.infrastructure.api.dto;

import account.domain.model.Provider;

public record SocialAuthRequest(
        Provider provider,
        String providerId,
        String displayName,
        String email,
        String photoUrl
) {
}
