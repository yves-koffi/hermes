package account.infrastructure.api.dto;

public record LoginRequest(
        String email,
        String password
) {
}
