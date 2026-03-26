package account.application.usecase;

import account.application.command.CheckTokenCommand;
import account.application.result.CheckTokenResult;
import io.smallrye.mutiny.Uni;

/**
 * Use case technique de validation d'un token métier stocké sous forme de hash.
 *
 * Il sert à confirmer qu'un token existe, qu'il est du bon type, qu'il n'est pas révoqué
 * et qu'il n'est pas expiré. Il ne doit pas porter à lui seul l'intention métier complète
 * du flux appelant; il fournit seulement une réponse booléenne sur la validité courante.
 */
public interface CheckTokenUseCase {
    Uni<CheckTokenResult> execute(CheckTokenCommand command);
}
