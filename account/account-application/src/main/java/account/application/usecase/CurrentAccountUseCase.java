package account.application.usecase;

import account.application.result.AccountDetails;
import io.smallrye.mutiny.Uni;

/**
 * Use case de consultation du compte actuellement authentifié.
 *
 * L'identité du compte est dérivée du contexte d'exécution de la requête. L'implémentation
 * retourne une vue détaillée du compte utile pour l'écran "profil" ou pour initialiser
 * un client après authentification.
 */
public interface CurrentAccountUseCase {
    Uni<AccountDetails> execute();
}
