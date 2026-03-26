package account.application.usecase;

import account.application.command.ForgetPasswordCommand;
import account.application.result.ForgetPasswordResult;
import io.smallrye.mutiny.Uni;

/**
 * Use case de demande de réinitialisation de mot de passe.
 *
 * Il génère un token ou un code de réinitialisation associé au compte ciblé,
 * l'enregistre sous forme sécurisée en base et déclenche l'envoi de la notification.
 * Ce flux prépare le reset mais ne change pas encore le mot de passe.
 */
public interface ForgetPasswordUseCase {
    Uni<ForgetPasswordResult> execute(ForgetPasswordCommand command);
}
