package account.application.service;

import account.application.result.AccountSecurityEventResult;
import account.application.spi.AccountSecurityEventRepository;
import account.application.usecase.SecurityHistoryUseCase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class SecurityHistoryService implements SecurityHistoryUseCase {

    @Inject
    CurrentAuthenticatedAccountService currentAuthenticatedAccountService;
    @Inject
    AccountSecurityEventRepository accountSecurityEventRepository;

    @Override
    public Uni<List<AccountSecurityEventResult>> execute() {
        return currentAuthenticatedAccountService.requireCurrentAccount()
                .flatMap(account -> accountSecurityEventRepository.findRecentByAccountId(account.id(), 50))
                .map(events -> events.stream()
                        .map(event -> new AccountSecurityEventResult(
                                event.id(),
                                event.eventType(),
                                event.detail(),
                                event.ipAddress(),
                                event.occurredAt()
                        ))
                        .toList());
    }
}
