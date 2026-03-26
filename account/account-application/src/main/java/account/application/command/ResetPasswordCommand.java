package account.application.command;

public record ResetPasswordCommand(
        String token,
        String newPassword,
        String confirmNewPassword
) {
}
