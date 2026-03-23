package account.infrastructure.api.dto;

public record ChangePasswordRequest(
        String currentPassword,
        String newPassword,
        String confirmNewPassword
) {
}
