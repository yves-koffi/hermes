package business.card.application.service;

import business.card.application.mapper.AccountResultMapper;
import business.card.application.result.AccountDetails;
import business.card.application.spi.AccountRepository;
import business.card.application.spi.CurrentAccountProvider;
import business.card.application.usecase.CurrentAccountUseCase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import shared.domain.exception.DomainNotFoundException;

import java.util.Map;

@ApplicationScoped
public class CurrentAccountService implements CurrentAccountUseCase {

    @Inject
    CurrentAccountProvider currentAccountProvider;
    @Inject
    AccountRepository accountRepository;
    @Inject
    AccountResultMapper accountResultMapper;

    @Override
    public Uni<AccountDetails> execute() {
        return currentAccountProvider.getCurrentAccountId()
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
