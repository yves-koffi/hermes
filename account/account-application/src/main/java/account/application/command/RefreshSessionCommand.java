package account.application.command;

public record RefreshSessionCommand(
        String refreshToken
) {
}
