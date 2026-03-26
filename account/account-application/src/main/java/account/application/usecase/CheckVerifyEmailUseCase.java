package account.application.usecase;

import account.application.command.VerifyAccountCommand;
import account.application.result.AccountVerificationResult;
import io.smallrye.mutiny.Uni;

/**
 * Use case de validation d'adresse email.
 *
 * Il consomme un token de vérification, confirme qu'il est encore valide et active
 * le compte associé. Ce flux finalise l'onboarding d'un compte basic avant le premier
 * usage complet de l'authentification.
 */
public interface CheckVerifyEmailUseCase {
    Uni<AccountVerificationResult> execute(VerifyAccountCommand command);
}
