package account.application.result;

public record AuthDetails(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessExpiresIn,
        long refreshExpiresIn
) {
}
