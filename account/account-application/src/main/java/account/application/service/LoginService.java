package account.application.service;

import account.application.command.LoginCommand;
import account.application.result.AuthResult;
import account.application.spi.AccountRepository;
import account.application.usecase.LoginUseCase;
import account.domain.model.Account;
import account.domain.model.Provider;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import shared.domain.exception.AuthenticationException;

/**
 * Implémentation du use case d'authentification locale.
 *
 * Le service recherche le compte par email, vérifie que le provider est compatible avec
 * une authentification par mot de passe, contrôle la validité du secret soumis puis refuse
 * les comptes basic non encore activés. En cas de succès, il délègue à la gestion de session
 * la création de la session serveur et l'émission des tokens.
 */
@ApplicationScoped
public class LoginService implements LoginUseCase {

    private static final Logger LOGGER = Logger.getLogger(LoginService.class);

    @Inject
    AccountRepository accountRepository;
    @Inject
    AuthSessionTokenService authSessionTokenService;
    @Inject
    AccountMetrics accountMetrics;

    @Override
    public Uni<AuthResult> execute(LoginCommand command) {
        return accountRepository.findByEmail(command.email())
                .flatMap(accountOpt -> {
                    if (accountOpt.isEmpty()) {
                        return failInvalidCredentials(command.email(), "account_not_found");
                    }

                    var account = accountOpt.get();
                    if (!isBasicPasswordValid(account, command.password())) {
                        return failInvalidCredentials(command.email(), "invalid_password_or_provider");
                    }
                    if (account.isDisabled()) {
                        accountMetrics.recordLoginFailed();
                        LOGGER.warnv("event=account_login_refused reason=account_disabled email={0}", account.email());
                        return Uni.createFrom().failure(AuthenticationException.accountDisabled(account.email()));
                    }

                    if (!account.isActivated()) {
                        accountMetrics.recordLoginFailed();
                        LOGGER.warnv("event=account_login_refused reason=account_unverified email={0}", account.email());
                        return Uni.createFrom().failure(AuthenticationException.accountUnverified(account.email()));
                    }

                    return authSessionTokenService.issueTokens(account, null)
                            .invoke(ignored -> {
                                accountMetrics.recordLoginSucceeded();
                                LOGGER.infov("event=account_login_succeeded accountId={0} email={1}", account.id(), account.email());
                            });
                });
    }

    private boolean isBasicPasswordValid(Account account, String password) {
        if (account.provider() != Provider.BASIC) {
            return false;
        }
        return account.password() != null && BcryptUtil.matches(password, account.password());
    }

    private Uni<AuthResult> failInvalidCredentials(String email, String reason) {
        accountMetrics.recordLoginFailed();
        LOGGER.warnv("event=account_login_refused reason={0} email={1}", reason, email);
        return Uni.createFrom().failure(AuthenticationException.invalidCredentials());
    }
}
