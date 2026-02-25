package business.card.infrastructure.api.dto;

public record LoginRequest(
        String provider_id,
        String displayName,
        String email,
        String id,
        String photoUrl
) {
}
