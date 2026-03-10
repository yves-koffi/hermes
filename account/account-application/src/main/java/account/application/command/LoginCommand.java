package account.application.command;

public record LoginCommand(
        String email,
        String password
) {
}
