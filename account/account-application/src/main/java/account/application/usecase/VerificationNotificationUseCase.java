package account.application.usecase;

import account.application.command.SendVerifyCodeCommand;
import account.application.result.VerifyCodeSentResult;
import io.smallrye.mutiny.Uni;

/**
 * Use case d'émission d'une notification de vérification d'email.
 *
 * Il prépare un code ou un lien de vérification, l'enregistre en base sous forme de hash
 * puis envoie le contenu correspondant au compte ciblé. Il est utilisé après inscription
 * ou lors d'une demande explicite de renvoi.
 */
public interface VerificationNotificationUseCase {
    Uni<VerifyCodeSentResult> execute(SendVerifyCodeCommand command);
}
