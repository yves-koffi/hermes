package shared.domain.model;

public record TokenPair(
        String accessToken,
        String refreshToken,
        String tokenType
) {
}
