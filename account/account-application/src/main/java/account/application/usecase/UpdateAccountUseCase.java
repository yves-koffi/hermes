package account.application.usecase;

import account.application.command.UpdateAccountCommand;
import io.smallrye.mutiny.Uni;

/**
 * Use case de mise à jour des informations modifiables du compte courant.
 *
 * Ce contrat couvre les données de profil qui peuvent être modifiées sans changer
 * l'identité d'authentification ou l'état de session du compte.
 */
public interface UpdateAccountUseCase {
    Uni<Void> execute(UpdateAccountCommand command);
}
