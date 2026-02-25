package business.card.infrastructure.api.dto;

public record AccountResponse(
        String name,
        String email,
        String token,
        String provider,
        String photoUrl,
        Boolean activated,
        Integer created_at
) {
}
