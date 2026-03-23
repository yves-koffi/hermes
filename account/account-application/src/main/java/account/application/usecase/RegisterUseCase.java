package account.application.usecase;

import account.application.command.RegisterCommand;
import account.application.result.RegisterDetails;
import io.smallrye.mutiny.Uni;

/**
 * Use case d'inscription d'un nouveau compte local.
 *
 * Il crée un compte basic non encore vérifié, garantit l'unicité de l'email et
 * déclenche ensuite le mécanisme de vérification d'adresse email. Le résultat permet
 * au client de savoir si une étape de vérification est requise.
 */
public interface RegisterUseCase {
    Uni<RegisterDetails> execute(RegisterCommand command);
}
