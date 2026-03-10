package account.application.command;

public record CheckVerificationNotificationCodeCommand(
        String email,
        String code
) {
}
