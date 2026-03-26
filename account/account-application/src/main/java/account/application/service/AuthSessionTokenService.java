package account.application.service;

import account.application.result.AuthResult;
import account.application.spi.AuthSessionRepository;
import account.domain.model.Account;
import account.domain.model.AuthSession;
import account.domain.model.Provider;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import shared.application.context.RequestContext;
import shared.application.spi.JwtTokenProvider;
import shared.domain.model.TokenType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service applicatif de bas niveau dédié à la gestion des tokens de session.
 *
 * Il centralise la création des sessions serveur, l'émission des JWT d'accès et de refresh,
 * le parsing du refresh token ainsi que la révocation d'une session persistée. Ce composant
 * supporte les use cases de login, refresh, logout et toute opération qui doit invalider
 * ou régénérer une session.
 */
@ApplicationScoped
public class AuthSessionTokenService {

    @Inject
    AuthSessionRepository authSessionRepository;
    @Inject
    JwtTokenProvider jwtTokenProvider;
    @Inject
    RequestContext context;
    @Inject
    JWTParser jwtParser;

    @ConfigProperty(name = "jwt.access-token-validity", defaultValue = "120")
    long accessTokenValidity;

    @ConfigProperty(name = "jwt.refresh-token-validity", defaultValue = "10080")
    long refreshTokenValidity;

    public Uni<AuthResult> issueTokens(Account account, UUID rotatedFromSessionId) {
        UUID sessionId = UUID.randomUUID();
        String rawRefreshToken = UUID.randomUUID().toString();
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime accessExpiresAt = now.plusMinutes(accessTokenValidity);
        OffsetDateTime refreshExpiresAt = now.plusMinutes(refreshTokenValidity);
        AuthSession session = new AuthSession(
                sessionId,
                account.id(),
                BcryptUtil.bcryptHash(rawRefreshToken),
                refreshExpiresAt,
                context.getExecutionContext() != null ? context.getExecutionContext().ip() : null,
                null,
                rotatedFromSessionId,
                null,
                null,
                null,
                null
        );

        return authSessionRepository.save(session)
                .map(saved -> new AuthResult(
                        account.id(),
                        account.isActivated() || account.provider() != Provider.BASIC,
                        jwtTokenProvider.generateAccessToken(
                                account.email(),
                                account.id(),
                                List.of("USER"),
                                accessTokenValidity
                        ),
                        jwtTokenProvider.generateRefreshToken(
                                buildRefreshSubject(saved.id(), rawRefreshToken),
                                account.id(),
                                refreshTokenValidity
                        ),
                        "Bearer",
                        accessTokenValidity,
                        refreshTokenValidity,
                        accessExpiresAt,
                        refreshExpiresAt
                ));
    }

    public Optional<ParsedRefreshToken> parseRefreshToken(String refreshToken) {
        try {
            JsonWebToken jwt = jwtParser.parse(refreshToken);
            String type = jwt.getClaim("type");
            if (!TokenType.REFRESH_TOKEN.value.equals(type)) {
                return Optional.empty();
            }
            return parseRefreshSubject(jwt.getSubject());
        } catch (ParseException e) {
            return Optional.empty();
        }
    }

    public boolean matches(AuthSession session, ParsedRefreshToken parsedToken) {
        return session != null
                && parsedToken != null
                && session.id().equals(parsedToken.sessionId())
                && session.refreshTokenHash() != null
                && BcryptUtil.matches(parsedToken.rawToken(), session.refreshTokenHash());
    }

    public Uni<AuthSession> revoke(AuthSession session, OffsetDateTime revokedAt) {
        AuthSession revokedSession = new AuthSession(
                session.id(),
                session.accountId(),
                session.refreshTokenHash(),
                session.expiryDate(),
                session.ipAddress(),
                session.userAgent(),
                session.rotatedFromSessionId(),
                session.lastUsedAt(),
                revokedAt,
                session.createdAt(),
                revokedAt
        );
        return authSessionRepository.save(revokedSession);
    }

    private String buildRefreshSubject(UUID sessionId, String rawRefreshToken) {
        return sessionId + ":" + rawRefreshToken;
    }

    private Optional<ParsedRefreshToken> parseRefreshSubject(String subject) {
        if (subject == null || subject.isBlank()) {
            return Optional.empty();
        }
        int separatorIndex = subject.indexOf(':');
        if (separatorIndex <= 0 || separatorIndex >= subject.length() - 1) {
            return Optional.empty();
        }
        try {
            return Optional.of(new ParsedRefreshToken(
                    UUID.fromString(subject.substring(0, separatorIndex)),
                    subject.substring(separatorIndex + 1)
            ));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public record ParsedRefreshToken(
            UUID sessionId,
            String rawToken
    ) {
    }
}
