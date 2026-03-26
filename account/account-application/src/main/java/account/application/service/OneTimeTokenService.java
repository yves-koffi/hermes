package account.application.service;

import account.domain.model.TokenType;
import jakarta.enterprise.context.ApplicationScoped;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@ApplicationScoped
public class OneTimeTokenService {

    // Les tokens one-shot sont stockés hashés en base pour éviter de persister leur valeur brute.
    public String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    // Les parcours par code utilisent un code court ; les liens utilisent un token opaque plus long.
    public String generateToken(TokenType tokenType) {
        return switch (tokenType) {
            case EMAIL_VERIFICATION_CODE, PASSWORD_RESET_CODE ->
                    String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
            default -> UUID.randomUUID().toString();
        };
    }

    // Les durées de vie restent centralisées ici pour garder une politique cohérente entre services.
    public OffsetDateTime computeExpiryDate(TokenType tokenType) {
        OffsetDateTime now = OffsetDateTime.now();
        return switch (tokenType) {
            case EMAIL_VERIFICATION_CODE, PASSWORD_RESET_CODE -> now.plusMinutes(10);
            case EMAIL_VERIFICATION_LINK, PASSWORD_RESET_LINK -> now.plusHours(24);
            case SESSION_REFRESH -> now.plusDays(30);
            default -> throw new IllegalArgumentException("Unsupported token type: " + tokenType);
        };
    }
}
