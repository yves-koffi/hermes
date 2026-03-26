package account.application.usecase;

import account.application.command.ResetPasswordCommand;
import account.application.result.PasswordResetResult;
import io.smallrye.mutiny.Uni;

/**
 * Use case de finalisation d'une réinitialisation de mot de passe.
 *
 * Il consomme un token de reset déjà émis, vérifie son état, met à jour le mot de passe
 * du compte puis invalide les sessions actives selon la politique de sécurité du module.
 */
public interface ResetPasswordUseCase {
    Uni<PasswordResetResult> execute(ResetPasswordCommand command);
}
