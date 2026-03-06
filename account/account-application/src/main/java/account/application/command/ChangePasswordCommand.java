package account.application.command;

public record ChangePasswordCommand(
        String currentPassword,
        String newPassword,
        String token,
        String ipAddress
) {
}
