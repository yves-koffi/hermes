package account.application.service;

import account.application.spi.AccountRepository;
import account.domain.model.Account;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import shared.application.context.RequestContext;
import shared.domain.exception.AuthenticationException;
import shared.domain.exception.DomainNotFoundException;

import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class CurrentAuthenticatedAccountService {

    @Inject
    RequestContext context;
    @Inject
    AccountRepository accountRepository;

    public Uni<Account> requireCurrentAccount() {
        UUID accountId = currentAccountId();
        return accountRepository.findById(accountId)
                .flatMap(accountOpt -> {
                    if (accountOpt.isPresent()) {
                        return Uni.createFrom().item(accountOpt.get());
                    }
                    return Uni.createFrom().failure(
                            new DomainNotFoundException(
                                    "ACCOUNT_NOT_FOUND",
                                    "account.not_found",
                                    Map.of("id", accountId)
                            )
                    );
                });
    }

    public UUID currentAccountId() {
        if (context.getExecutionContext() == null || context.getExecutionContext().accountId() == null) {
            throw AuthenticationException.invalidCredentials();
        }
        return context.getExecutionContext().accountId();
    }
}
