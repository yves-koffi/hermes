package account.application.service;


import account.application.mapper.AccountResultMapper;
import account.application.result.AccountDetails;
import account.application.spi.AccountRepository;
import account.application.usecase.CurrentAccountUseCase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import shared.application.context.RequestContext;
import shared.domain.exception.DomainNotFoundException;

import java.util.Map;

/**
 * Implémentation du use case de lecture du compte courant.
 *
 * Le service s'appuie sur l'identifiant présent dans le contexte d'exécution,
 * recharge le compte depuis le repository et le projette vers un résultat de lecture.
 * Il échoue explicitement si le compte référencé par le contexte n'existe plus.
 */
@ApplicationScoped
public class CurrentAccountService implements CurrentAccountUseCase {

    @Inject
    RequestContext context;
    @Inject
    AccountRepository accountRepository;
    @Inject
    AccountResultMapper accountResultMapper;

    @Override
    public Uni<AccountDetails> execute() {
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
                    return Uni.createFrom().item(accountResultMapper.toDetails(accountOpt.get()));
                });
    }
}
