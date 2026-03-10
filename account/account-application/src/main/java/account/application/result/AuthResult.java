package account.application.result;

public record AuthResult(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessExpiresIn,
        long refreshExpiresIn
) {
}
