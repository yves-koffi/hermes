package shared.application.spi;


import java.util.List;
import java.util.UUID;

public interface JwtTokenProvider {
    String generateAccessToken(
            String subjectId,
            String userId,
            List<String> roles,
            Long expiresIn
    );

    String generateRefreshToken(
            String hashToken,
            UUID userId,
            Long expiresIn
    );
}
