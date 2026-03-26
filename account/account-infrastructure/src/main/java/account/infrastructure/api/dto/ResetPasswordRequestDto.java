package account.infrastructure.api.dto;

public record ResetPasswordRequestDto(
        String token,
        String newPassword,
        String confirmNewPassword
) {
}
