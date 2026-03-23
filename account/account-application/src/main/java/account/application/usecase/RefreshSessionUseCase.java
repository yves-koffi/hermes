package account.application.usecase;

import account.application.command.RefreshSessionCommand;
import account.application.result.AuthDetails;
import io.smallrye.mutiny.Uni;

/**
 * Use case de renouvellement de session via refresh token.
 *
 * Il vérifie le refresh token, contrôle la session associée, applique la rotation du token
 * côté serveur puis retourne une nouvelle paire d'access/refresh tokens. Ce flux est
 * central pour les clients web et mobile qui maintiennent des sessions longues.
 */
public interface RefreshSessionUseCase {
    Uni<AuthDetails> execute(RefreshSessionCommand command);
}
