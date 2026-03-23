package account.application.usecase;

import io.smallrye.mutiny.Uni;

/**
 * Use case de révocation de toutes les sessions du compte courant.
 *
 * Ce flux est utilisé pour déconnecter l'utilisateur de tous ses appareils ou pour
 * appliquer une mesure de sécurité après un changement sensible sur le compte.
 */
public interface LogoutAllSessionsUseCase {
    Uni<Void> execute();
}
