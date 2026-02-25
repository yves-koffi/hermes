package business.card.application.result;

public record AccountSummary(
        String id,
        String name,
        String email,
        String provider,
        Boolean activated
) {
}