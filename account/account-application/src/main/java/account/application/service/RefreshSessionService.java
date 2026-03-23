package account.application.service;

import account.application.command.RefreshSessionCommand;
import account.application.result.AuthDetails;
import account.application.spi.AccountRepository;
import account.application.spi.AuthSessionRepository;
import account.application.usecase.RefreshSessionUseCase;
import account.domain.model.Provider;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import shared.domain.exception.AuthenticationException;

import java.time.OffsetDateTime;

/**
 * Implémentation du use case de renouvellement de session.
 *
 * Le service valide le refresh token reçu, recharge la session associée, refuse les sessions
 * expirées ou révoquées puis recharge le compte pour vérifier son état actuel. Quand toutes
 * les conditions sont réunies, l'ancienne session est révoquée et une nouvelle paire de tokens
 * est émise selon une stratégie de rotation.
 */
@ApplicationScoped
public class RefreshSessionService implements RefreshSessionUseCase {

    @Inject
    AuthSessionRepository authSessionRepository;
    @Inject
    AccountRepository accountRepository;
    @Inject
    AuthSessionTokenService authSessionTokenService;

    @Override
    public Uni<AuthDetails> execute(RefreshSessionCommand command) {
        if (command.refreshToken() == null || command.refreshToken().isBlank()) {
            return Uni.createFrom().failure(AuthenticationException.invalidCredentials());
        }

        var parsedToken = authSessionTokenService.parseRefreshToken(command.refreshToken())
                .orElse(null);
        if (parsedToken == null) {
            return Uni.createFrom().failure(AuthenticationException.invalidCredentials());
        }

        return authSessionRepository.findById(parsedToken.sessionId())
                .flatMap(sessionOpt -> {
                    if (sessionOpt.isEmpty() || !authSessionTokenService.matches(sessionOpt.get(), parsedToken)) {
                        return Uni.createFrom().failure(AuthenticationException.invalidCredentials());
                    }

                    var session = sessionOpt.get();
                    if (session.revokedAt() != null || session.expiryDate().isBefore(OffsetDateTime.now())) {
                        return Uni.createFrom().failure(AuthenticationException.invalidCredentials());
                    }

                    return accountRepository.findById(session.accountId())
                            .flatMap(accountOpt -> {
                                if (accountOpt.isEmpty()) {
                                    return Uni.createFrom().failure(AuthenticationException.invalidCredentials());
                                }

                                var account = accountOpt.get();
                                if (!account.isActivated() && account.provider() == Provider.BASIC) {
                                    return Uni.createFrom().failure(AuthenticationException.accountUnverified(account.email()));
                                }

                                OffsetDateTime now = OffsetDateTime.now();
                                return authSessionTokenService.revoke(session, now)
                                        .flatMap(revoked -> authSessionTokenService.issueTokens(account, revoked.id()));
                            });
                });
    }
}
