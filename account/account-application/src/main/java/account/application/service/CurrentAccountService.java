package account.application.service;


import account.application.mapper.AccountResultMapper;
import account.application.result.AccountDetails;
import account.application.spi.AccountRepository;
import account.application.usecase.CurrentAccountUseCase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import shared.application.context.ExecutionContext;
import shared.domain.exception.DomainNotFoundException;

import java.util.Map;

@ApplicationScoped
public class CurrentAccountService implements CurrentAccountUseCase {

    @Inject
    ExecutionContext context;
    @Inject
    AccountRepository accountRepository;
    @Inject
    AccountResultMapper accountResultMapper;

    @Override
    public Uni<AccountDetails> execute() {
        return context.getCurrentAccountId()
                .flatMap(accountId -> accountRepository.findById(accountId)
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
                            return Uni.createFrom().item(accountResultMapper.toDetails(accountOpt.get()));
                        }));
    }
}
