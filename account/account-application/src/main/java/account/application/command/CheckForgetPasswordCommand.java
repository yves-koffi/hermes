package account.application.command;

public record CheckForgetPasswordCommand(
        String email,
        String code
) {
}
