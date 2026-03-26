package account.infrastructure.api.dto;

public record LogoutRequestDto(
        String refreshToken
) {
}
