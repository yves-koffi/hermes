package account.infrastructure.api.dto;

public record ChangePasswordRequestDto(
        String currentPassword,
        String newPassword,
        String confirmNewPassword
) {
}
