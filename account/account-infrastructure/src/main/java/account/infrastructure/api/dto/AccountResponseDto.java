package account.infrastructure.api.dto;

public record AccountResponseDto(
        String id,
        String name,
        String email,
        String prefix,
        String number,
        String provider,
        String avatarUrl,
        Boolean activated,
        Boolean disabled,
        String createdAt,
        String updatedAt
) {
}
