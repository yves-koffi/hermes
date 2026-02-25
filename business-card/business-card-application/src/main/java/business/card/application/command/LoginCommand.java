package business.card.application.command;

public record LoginCommand(
        String provider_id,
        String displayName,
        String email,
        String id,
        String photoUrl
) {
}