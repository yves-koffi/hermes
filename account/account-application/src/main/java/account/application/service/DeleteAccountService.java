package account.application.service;

import account.application.spi.AccountRepository;
import account.application.spi.AuthSessionRepository;
import account.application.spi.HashTokenRepository;
import account.application.usecase.DeleteAccountUseCase;
import account.domain.model.AccountSecurityEventType;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;

@ApplicationScoped
public class DeleteAccountService implements DeleteAccountUseCase {

    @Inject
    CurrentAuthenticatedAccountService currentAuthenticatedAccountService;
    @Inject
    AccountRepository accountRepository;
    @Inject
    AuthSessionRepository authSessionRepository;
    @Inject
    HashTokenRepository hashTokenRepository;
    @Inject
    AccountSecurityEventService accountSecurityEventService;

    @Override
    public Uni<Void> execute() {
        return currentAuthenticatedAccountService.requireCurrentAccount()
                .flatMap(account -> accountSecurityEventService.record(
                                account,
                                AccountSecurityEventType.ACCOUNT_DELETED,
                                "Account deleted"
                        )
                        .flatMap(ignored -> authSessionRepository.revokeAllByAccountId(account.id(), OffsetDateTime.now()))
                        .flatMap(ignored -> hashTokenRepository.deleteByAccountId(account.id()))
                        .flatMap(ignored -> accountRepository.deleteById(account.id())));
    }
}
