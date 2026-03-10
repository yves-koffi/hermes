package account.application.command;

public record ResetPasswordCommand(
        String hashToken,
        String newPassword,
        String confirmNewPassword
) {
}
