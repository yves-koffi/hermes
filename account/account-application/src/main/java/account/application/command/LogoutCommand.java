package account.application.command;

public record LogoutCommand(
        String refreshToken
) {
}
