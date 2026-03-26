package account.infrastructure.api.dto;

import account.domain.model.Provider;

public record SocialAuthRequestDto(
        Provider provider,
        String providerId,
        String displayName,
        String email,
        String photoUrl
) {
}
