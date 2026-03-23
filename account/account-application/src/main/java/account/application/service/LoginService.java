package account.application.service;

import account.application.command.LoginCommand;
import account.application.result.AuthDetails;
import account.application.spi.AccountRepository;
import account.application.usecase.LoginUseCase;
import account.domain.model.Account;
import account.domain.model.Provider;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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

    @Inject
    AccountRepository accountRepository;
    @Inject
    AuthSessionTokenService authSessionTokenService;

    @Override
    public Uni<AuthDetails> execute(LoginCommand command) {
        return accountRepository.findByEmail(command.email())
                .flatMap(accountOpt -> {
                    if (accountOpt.isEmpty()) {
                        return Uni.createFrom().failure(AuthenticationException.invalidCredentials());
                    }

                    var account = accountOpt.get();
                    if (!isBasicPasswordValid(account, command.password())) {
                        return Uni.createFrom().failure(AuthenticationException.invalidCredentials());
                    }

                    if (!account.isActivated()) {
                        return Uni.createFrom().failure(AuthenticationException.accountUnverified(account.email()));
                    }

                    return authSessionTokenService.issueTokens(account, null);
                });
    }

    private boolean isBasicPasswordValid(Account account, String password) {
        if (account.provider() != Provider.BASIC) {
            return false;
        }
        return account.password() != null && BcryptUtil.matches(password, account.password());
    }
}
