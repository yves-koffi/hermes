package account.application.service;

import account.application.command.UpdateAccountCommand;
import account.application.spi.AccountRepository;
import account.application.usecase.UpdateAccountUseCase;
import account.domain.model.Account;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import shared.application.context.RequestContext;
import shared.domain.exception.DomainNotFoundException;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Implémentation du use case de mise à jour du profil courant.
 *
 * Le service relit le compte du contexte, fusionne les données modifiables reçues dans la
 * commande avec les données existantes puis persiste la nouvelle version. Il ne modifie pas
 * les éléments liés à l'authentification comme l'email, le provider ou le mot de passe.
 */
@ApplicationScoped
public class UpdateAccountService implements UpdateAccountUseCase {

    @Inject
    RequestContext context;
    @Inject
    AccountRepository accountRepository;

    @Override
    public Uni<Void> execute(UpdateAccountCommand command) {
        UUID accountId = context.getExecutionContext().accountId();
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

                    Account current = accountOpt.get();
                    Account updated = new Account(
                            current.id(),
                            command.name(),
                            current.email(),
                            command.phoneNumber(),
                            current.password(),
                            current.avatarUrl(),
                            current.providerId(),
                            current.provider(),
                            current.activatedAt(),
                            current.createdAt(),
                            OffsetDateTime.now()
                    );

                    return accountRepository.save(updated).replaceWithVoid();
                });
    }
}
