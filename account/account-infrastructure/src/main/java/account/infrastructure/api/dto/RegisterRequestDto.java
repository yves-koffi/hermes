package account.infrastructure.api.dto;

public record RegisterRequestDto(
        String name,
        String email,
        String password,
        String prefix,
        String number,
        boolean requiredVerifyEmail
) {
}
