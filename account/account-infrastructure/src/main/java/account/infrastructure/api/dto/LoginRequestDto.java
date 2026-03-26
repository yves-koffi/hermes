package account.infrastructure.api.dto;

public record LoginRequestDto(
        String email,
        String password
) {
}
