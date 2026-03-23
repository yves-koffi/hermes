package account.application.usecase;

import account.application.command.ChangePasswordCommand;
import io.smallrye.mutiny.Uni;

/**
 * Use case de modification du mot de passe pour un compte authentifié.
 *
 * Ce contrat couvre le scénario où l'utilisateur connaît déjà son mot de passe courant
 * et souhaite le remplacer par un nouveau mot de passe. L'implémentation doit valider
 * le mot de passe courant, vérifier la cohérence du nouveau mot de passe et invalider
 * les sessions actives si la politique de sécurité l'exige.
 */
public interface ChangePasswordUseCase {
    Uni<Void> execute(ChangePasswordCommand command);
}
