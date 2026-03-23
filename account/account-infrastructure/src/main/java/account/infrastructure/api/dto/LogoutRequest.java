package account.infrastructure.api.dto;

public record LogoutRequest(
        String refreshToken
) {
}
