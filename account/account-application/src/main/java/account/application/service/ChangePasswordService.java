package account.application.service;

import account.application.command.ChangePasswordCommand;
import account.application.spi.AccountRepository;
import account.application.spi.AuthSessionRepository;
import account.application.usecase.ChangePasswordUseCase;
import account.domain.model.AccountSecurityEventType;
import account.domain.model.Provider;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import shared.domain.exception.DomainConflictException;

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
    CurrentAuthenticatedAccountService currentAuthenticatedAccountService;
    @Inject
    AccountRepository accountRepository;
    @Inject
    AuthSessionRepository authSessionRepository;
    @Inject
    AccountSecurityEventService accountSecurityEventService;

    @Override
    public Uni<Void> execute(ChangePasswordCommand command) {
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

        return currentAuthenticatedAccountService.requireCurrentAccount()
                .flatMap(account -> {
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
                        return Uni.createFrom().failure(shared.domain.exception.AuthenticationException.invalidCredentials());
                    }

                    OffsetDateTime now = OffsetDateTime.now();
                    return accountRepository
                            .save(account.withPasswordHash(BcryptUtil.bcryptHash(command.newPassword()), now))
                            .flatMap(saved -> authSessionRepository.revokeAllByAccountId(saved.id(), now)
                                    .flatMap(ignored -> accountSecurityEventService.record(
                                            saved,
                                            AccountSecurityEventType.PASSWORD_CHANGED,
                                            "Password changed and active sessions revoked"
                                    )));
                });
    }
}
