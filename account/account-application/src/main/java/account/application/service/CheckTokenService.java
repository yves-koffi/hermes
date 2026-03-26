package account.application.service;

import account.application.command.CheckTokenCommand;
import account.application.result.CheckTokenResult;
import account.application.spi.HashTokenRepository;
import account.application.usecase.CheckTokenUseCase;
import account.domain.model.HashToken;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;

/**
 * Implémentation technique de validation d'un token one-shot.
 *
 * Le service convertit la valeur brute reçue en hash SHA-256, recherche l'entrée
 * correspondante en base puis vérifie son type, son état de révocation et sa date
 * d'expiration. Il ne réalise aucune transition métier; il expose uniquement un état
 * booléen de validité.
 */
@ApplicationScoped
public class CheckTokenService implements CheckTokenUseCase {

    @Inject
    HashTokenRepository hashTokenRepository;
    @Inject
    OneTimeTokenService oneTimeTokenService;

    @Override
    public Uni<CheckTokenResult> execute(CheckTokenCommand command) {
        if (command.token() == null || command.token().isBlank()) {
            return Uni.createFrom().item(new CheckTokenResult(false, null, null));
        }

        String hashToken = oneTimeTokenService.hash(command.token());
        return hashTokenRepository.findByHashToken(hashToken)
                .map(tokenOpt -> {
                    HashToken token = tokenOpt.orElse(null);
                    if (!isValid(token, command)) {
                        return new CheckTokenResult(false, null, null);
                    }
                    return new CheckTokenResult(true, token.tokenType().name(), token.expiryDate());
                });
    }

    private boolean isValid(HashToken token, CheckTokenCommand command) {
        if (token == null) {
            return false;
        }
        if (token.revokedAt() != null) {
            return false;
        }
        if (command.type() != null && token.tokenType() != command.type()) {
            return false;
        }
        return !token.expiryDate().isBefore(OffsetDateTime.now());
    }

}
