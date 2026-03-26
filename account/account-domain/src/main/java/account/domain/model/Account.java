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
        OffsetDateTime disabledAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public boolean isActivated() {
        return activatedAt != null;
    }

    public boolean isDisabled() {
        return disabledAt != null;
    }

    public Account withProfile(String nextName, PhoneNumber nextPhoneNumber, String nextAvatarUrl, OffsetDateTime now) {
        return new Account(
                id,
                nextName,
                email,
                nextPhoneNumber,
                password,
                nextAvatarUrl,
                providerId,
                provider,
                activatedAt,
                disabledAt,
                createdAt,
                now
        );
    }

    public Account withPasswordHash(String nextPasswordHash, OffsetDateTime now) {
        return new Account(
                id,
                name,
                email,
                phoneNumber,
                nextPasswordHash,
                avatarUrl,
                providerId,
                provider,
                activatedAt,
                disabledAt,
                createdAt,
                now
        );
    }

    public Account activate(OffsetDateTime verifiedAt, OffsetDateTime now) {
        return new Account(
                id,
                name,
                email,
                phoneNumber,
                password,
                avatarUrl,
                providerId,
                provider,
                verifiedAt,
                disabledAt,
                createdAt,
                now
        );
    }

    public Account changeEmail(String nextEmail, boolean requiresVerification, OffsetDateTime now) {
        return new Account(
                id,
                name,
                nextEmail,
                phoneNumber,
                password,
                avatarUrl,
                providerId,
                provider,
                requiresVerification ? null : activatedAt,
                disabledAt,
                createdAt,
                now
        );
    }

    public Account disable(OffsetDateTime disabledAt, OffsetDateTime now) {
        return new Account(
                id,
                name,
                email,
                phoneNumber,
                password,
                avatarUrl,
                providerId,
                provider,
                activatedAt,
                disabledAt,
                createdAt,
                now
        );
    }
}
