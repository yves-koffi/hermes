package account.application.service;

import account.application.command.CreateAccountCommand;
import account.application.command.SocialCredentialCommand;
import account.application.mapper.AccountCommandMapper;
import account.application.result.AuthResult;
import account.application.spi.AccountRepository;
import account.application.usecase.SocialAuthUseCase;
import account.domain.model.Account;
import account.domain.model.Provider;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import shared.domain.exception.AuthenticationException;
import shared.domain.exception.DomainConflictException;

import java.util.Map;

/**
 * Implémentation du use case d'authentification sociale.
 *
 * Le service vérifie d'abord si un compte existe déjà pour l'email fourni. Si oui, il contrôle
 * la cohérence du provider et du providerId avant d'ouvrir une session. Sinon, il construit
 * un nouveau compte social à partir d'une commande de création puis persiste ce compte comme
 * déjà activé. Dans les deux cas, il délègue ensuite l'émission des tokens à la gestion de session.
 */
@ApplicationScoped
public class SocialAuthService implements SocialAuthUseCase {

    @Inject
    AccountRepository accountRepository;
    @Inject
    AuthSessionTokenService authSessionTokenService;
    @Inject
    AccountCommandMapper accountCommandMapper;

    @Override
    public Uni<AuthResult> execute(SocialCredentialCommand command) {
        return accountRepository.findByEmail(command.email())
                .flatMap(accountOpt -> {
                    if (accountOpt.isPresent()) {
                        Account existing = accountOpt.get();
                        if (existing.isDisabled()) {
                            return Uni.createFrom().failure(AuthenticationException.accountDisabled(existing.email()));
                        }
                        if (existing.provider() == Provider.BASIC) {
                            return Uni.createFrom().failure(
                                    new DomainConflictException(
                                            "ACCOUNT_PROVIDER_MISMATCH",
                                            "account.social.provider.mismatch",
                                            Map.of("email", existing.email(), "provider", existing.provider().name())
                                    )
                            );
                        }
                        if (command.provider() != null && existing.provider() != command.provider()) {
                            return Uni.createFrom().failure(
                                    new DomainConflictException(
                                            "ACCOUNT_PROVIDER_MISMATCH",
                                            "account.social.provider.mismatch",
                                            Map.of("email", existing.email(), "provider", existing.provider().name())
                                    )
                            );
                        }
                        if (existing.providerId() != null
                                && command.providerId() != null
                                && !existing.providerId().equals(command.providerId())) {
                            return Uni.createFrom().failure(
                                    new DomainConflictException(
                                            "ACCOUNT_PROVIDER_ID_MISMATCH",
                                            "account.social.provider_id.mismatch",
                                            Map.of("email", existing.email())
                                    )
                            );
                        }
                        return authSessionTokenService.issueTokens(existing, null);
                    }

                    Account account = accountCommandMapper.toAccount(
                            new CreateAccountCommand(
                                    command.displayName(),
                                    command.email(),
                                    null,
                                    null,
                                    null,
                                    command.photoUrl(),
                                    command.providerId(),
                                    command.provider() == null ? Provider.GOOGLE : command.provider()
                            )
                    );

                    return accountRepository.save(account)
                            .flatMap(saved -> authSessionTokenService.issueTokens(saved, null));
                });
    }
}
