package account.application.service;

import account.application.command.RefreshSessionCommand;
import account.application.result.AuthResult;
import account.application.spi.AccountRepository;
import account.application.spi.AuthSessionRepository;
import account.application.usecase.RefreshSessionUseCase;
import account.domain.model.Provider;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
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

    private static final Logger LOGGER = Logger.getLogger(RefreshSessionService.class);

    @Inject
    AuthSessionRepository authSessionRepository;
    @Inject
    AccountRepository accountRepository;
    @Inject
    AuthSessionTokenService authSessionTokenService;

    @Override
    public Uni<AuthResult> execute(RefreshSessionCommand command) {
        if (command.refreshToken() == null || command.refreshToken().isBlank()) {
            LOGGER.warn("event=account_refresh_refused reason=missing_refresh_token");
            return Uni.createFrom().failure(AuthenticationException.invalidCredentials());
        }

        var parsedToken = authSessionTokenService.parseRefreshToken(command.refreshToken())
                .orElse(null);
        if (parsedToken == null) {
            LOGGER.warn("event=account_refresh_refused reason=invalid_refresh_token");
            return Uni.createFrom().failure(AuthenticationException.invalidCredentials());
        }

        return authSessionRepository.findById(parsedToken.sessionId())
                .flatMap(sessionOpt -> {
                    if (sessionOpt.isEmpty() || !authSessionTokenService.matches(sessionOpt.get(), parsedToken)) {
                        LOGGER.warnv("event=account_refresh_refused reason=session_not_found_or_mismatch sessionId={0}", parsedToken.sessionId());
                        return Uni.createFrom().failure(AuthenticationException.invalidCredentials());
                    }

                    var session = sessionOpt.get();
                    if (session.revokedAt() != null || session.expiryDate().isBefore(OffsetDateTime.now())) {
                        LOGGER.warnv("event=account_refresh_refused reason=session_revoked_or_expired sessionId={0}", session.id());
                        return Uni.createFrom().failure(AuthenticationException.invalidCredentials());
                    }

                    return accountRepository.findById(session.accountId())
                            .flatMap(accountOpt -> {
                                if (accountOpt.isEmpty()) {
                                    LOGGER.warnv("event=account_refresh_refused reason=account_not_found accountId={0}", session.accountId());
                                    return Uni.createFrom().failure(AuthenticationException.invalidCredentials());
                                }

                                var account = accountOpt.get();
                                if (account.isDisabled()) {
                                    LOGGER.warnv("event=account_refresh_refused reason=account_disabled accountId={0} email={1}", account.id(), account.email());
                                    return Uni.createFrom().failure(AuthenticationException.accountDisabled(account.email()));
                                }
                                if (!account.isActivated() && account.provider() == Provider.BASIC) {
                                    LOGGER.warnv("event=account_refresh_refused reason=account_unverified accountId={0} email={1}", account.id(), account.email());
                                    return Uni.createFrom().failure(AuthenticationException.accountUnverified(account.email()));
                                }

                                OffsetDateTime now = OffsetDateTime.now();
                                return authSessionTokenService.revoke(session, now)
                                        .flatMap(revoked -> authSessionTokenService.issueTokens(account, revoked.id()));
                            });
                });
    }
}
