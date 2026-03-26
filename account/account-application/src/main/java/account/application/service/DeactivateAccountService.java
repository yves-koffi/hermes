package account.application.service;

import account.application.spi.AccountRepository;
import account.application.spi.AuthSessionRepository;
import account.application.usecase.DeactivateAccountUseCase;
import account.domain.model.AccountSecurityEventType;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;

@ApplicationScoped
public class DeactivateAccountService implements DeactivateAccountUseCase {

    @Inject
    CurrentAuthenticatedAccountService currentAuthenticatedAccountService;
    @Inject
    AccountRepository accountRepository;
    @Inject
    AuthSessionRepository authSessionRepository;
    @Inject
    AccountSecurityEventService accountSecurityEventService;

    @Override
    public Uni<Void> execute() {
        return currentAuthenticatedAccountService.requireCurrentAccount()
                .flatMap(account -> {
                    if (account.isDisabled()) {
                        return Uni.createFrom().voidItem();
                    }

                    OffsetDateTime now = OffsetDateTime.now();
                    return accountRepository.save(account.disable(now, now))
                            .flatMap(saved -> authSessionRepository.revokeAllByAccountId(saved.id(), now)
                                    .flatMap(ignored -> accountSecurityEventService.record(
                                            saved,
                                            AccountSecurityEventType.ACCOUNT_DEACTIVATED,
                                            "Account deactivated and active sessions revoked"
                                    )));
                });
    }
}
