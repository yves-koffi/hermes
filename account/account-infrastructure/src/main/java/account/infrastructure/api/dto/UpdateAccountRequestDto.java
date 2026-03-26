package account.infrastructure.api.dto;

public record UpdateAccountRequestDto(
        String name,
        String prefix,
        String number,
        String avatarUrl
) {
}
