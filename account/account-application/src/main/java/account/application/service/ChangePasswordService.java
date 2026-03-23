package account.application.service;

import account.application.command.ChangePasswordCommand;
import account.application.spi.AccountRepository;
import account.application.spi.AuthSessionRepository;
import account.application.usecase.ChangePasswordUseCase;
import account.domain.model.Account;
import account.domain.model.Provider;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import shared.application.context.RequestContext;
import shared.domain.exception.AuthenticationException;
import shared.domain.exception.DomainConflictException;
import shared.domain.exception.DomainNotFoundException;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Implémentation du use case de changement de mot de passe pour le compte courant.
 *
 * Le service récupère l'utilisateur à partir du contexte de requête, vérifie que le compte
 * utilise bien le provider basic, valide le mot de passe courant puis remplace l'ancien hash
 * par un nouveau. Toutes les sessions actives sont ensuite révoquées pour éviter qu'un ancien
 * token reste utilisable après ce changement sensible.
 */
@ApplicationScoped
public class ChangePasswordService implements ChangePasswordUseCase {

    @Inject
    RequestContext context;
    @Inject
    AccountRepository accountRepository;
    @Inject
    AuthSessionRepository authSessionRepository;

    @Override
    public Uni<Void> execute(ChangePasswordCommand command) {
        if (context.getExecutionContext() == null || context.getExecutionContext().accountId() == null) {
            return Uni.createFrom().failure(AuthenticationException.invalidCredentials());
        }
        if (command.newPassword() == null || command.newPassword().isBlank()) {
            return Uni.createFrom().failure(
                    new DomainConflictException(
                            "INVALID_NEW_PASSWORD",
                            "account.change_password.new_password.invalid",
                            Map.of()
                    )
            );
        }
        if (!command.newPassword().equals(command.confirmNewPassword())) {
            return Uni.createFrom().failure(
                    new DomainConflictException(
                            "PASSWORD_CONFIRMATION_MISMATCH",
                            "account.change_password.password_confirmation.mismatch",
                            Map.of()
                    )
            );
        }

        var accountId = context.getExecutionContext().accountId();
        return accountRepository.findById(accountId)
                .flatMap(accountOpt -> {
                    if (accountOpt.isEmpty()) {
                        return Uni.createFrom().failure(
                                new DomainNotFoundException(
                                        "ACCOUNT_NOT_FOUND",
                                        "account.not_found",
                                        Map.of("id", accountId)
                                )
                        );
                    }

                    Account account = accountOpt.get();
                    if (account.provider() != Provider.BASIC) {
                        return Uni.createFrom().failure(
                                new DomainConflictException(
                                        "ACCOUNT_PROVIDER_NOT_SUPPORTED",
                                        "account.change_password.provider.not_supported",
                                        Map.of("provider", account.provider().name())
                                )
                        );
                    }
                    if (account.password() == null || !BcryptUtil.matches(command.currentPassword(), account.password())) {
                        return Uni.createFrom().failure(AuthenticationException.invalidCredentials());
                    }

                    OffsetDateTime now = OffsetDateTime.now();
                    Account updatedAccount = new Account(
                            account.id(),
                            account.name(),
                            account.email(),
                            account.phoneNumber(),
                            BcryptUtil.bcryptHash(command.newPassword()),
                            account.avatarUrl(),
                            account.providerId(),
                            account.provider(),
                            account.activatedAt(),
                            account.createdAt(),
                            now
                    );

                    return accountRepository.save(updatedAccount)
                            .flatMap(saved -> authSessionRepository.revokeAllByAccountId(saved.id(), now));
                });
    }
}
