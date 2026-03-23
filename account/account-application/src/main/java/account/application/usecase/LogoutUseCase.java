package account.application.usecase;

import account.application.command.LogoutCommand;
import io.smallrye.mutiny.Uni;

/**
 * Use case de déconnexion de la session portée par un refresh token.
 *
 * L'implémentation révoque la session serveur correspondante quand le token est valide.
 * Le flux doit rester idempotent pour permettre plusieurs appels de déconnexion sans erreur.
 */
public interface LogoutUseCase {
    Uni<Void> execute(LogoutCommand command);
}
