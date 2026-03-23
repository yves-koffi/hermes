package account.application.usecase;

import account.application.command.LoginCommand;
import account.application.result.AuthDetails;
import io.smallrye.mutiny.Uni;

/**
 * Use case d'authentification par email et mot de passe.
 *
 * L'implémentation vérifie l'existence du compte, la compatibilité du provider,
 * la validité du mot de passe et l'état d'activation du compte. En cas de succès,
 * elle crée la session d'authentification et retourne la paire de tokens attendue
 * par les clients.
 */
public interface LoginUseCase {
    Uni<AuthDetails> execute(LoginCommand command);
}
