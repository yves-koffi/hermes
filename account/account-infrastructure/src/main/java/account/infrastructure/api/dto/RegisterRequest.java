package account.infrastructure.api.dto;

public record RegisterRequest(
        String name,
        String email,
        String password,
        String prefix,
        String number
) {
}
