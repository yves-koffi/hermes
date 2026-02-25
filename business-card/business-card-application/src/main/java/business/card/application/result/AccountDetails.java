package business.card.application.result;

public record AccountDetails(
        String name,
        String email,
        String provider,
        String photoUrl,
        Boolean activated,
        Integer created_at
) {
}