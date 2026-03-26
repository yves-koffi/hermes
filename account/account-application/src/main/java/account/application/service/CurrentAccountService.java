package account.application.service;


import account.application.mapper.AccountResultMapper;
import account.application.result.AccountDetails;
import account.application.usecase.CurrentAccountUseCase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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
    CurrentAuthenticatedAccountService currentAuthenticatedAccountService;
    @Inject
    AccountResultMapper accountResultMapper;

    @Override
    public Uni<AccountDetails> execute() {
        return currentAuthenticatedAccountService.requireCurrentAccount()
                .map(accountResultMapper::toDetails);
    }
}
