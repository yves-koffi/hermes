package account.application.usecase;

import account.application.command.SocialCredentialCommand;
import account.application.result.AuthDetails;
import io.smallrye.mutiny.Uni;

/**
 * Use case d'authentification sociale.
 *
 * Il connecte un compte existant ou crée un nouveau compte social quand l'email n'existe pas
 * encore. L'implémentation doit contrôler la cohérence provider/email/providerId avant
 * d'ouvrir une session et d'émettre les tokens applicatifs.
 */
public interface SocialAuthUseCase {
    Uni<AuthDetails> execute(SocialCredentialCommand command);
}
