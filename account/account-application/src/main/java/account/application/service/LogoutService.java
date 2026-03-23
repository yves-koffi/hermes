package account.application.service;

import account.application.command.LogoutCommand;
import account.application.spi.AuthSessionRepository;
import account.application.usecase.LogoutUseCase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;

/**
 * Implémentation du use case de déconnexion ciblée sur une session.
 *
 * Le service parse le refresh token, recherche la session serveur correspondante,
 * vérifie la correspondance entre le token reçu et le hash stocké puis révoque la
 * session si elle est encore active. Le comportement reste volontairement idempotent.
 */
@ApplicationScoped
public class LogoutService implements LogoutUseCase {

    @Inject
    AuthSessionRepository authSessionRepository;
    @Inject
    AuthSessionTokenService authSessionTokenService;

    @Override
    public Uni<Void> execute(LogoutCommand command) {
        if (command.refreshToken() == null || command.refreshToken().isBlank()) {
            return Uni.createFrom().voidItem();
        }

        var parsedToken = authSessionTokenService.parseRefreshToken(command.refreshToken())
                .orElse(null);
        if (parsedToken == null) {
            return Uni.createFrom().voidItem();
        }

        return authSessionRepository.findById(parsedToken.sessionId())
                .flatMap(sessionOpt -> {
                    if (sessionOpt.isEmpty() || !authSessionTokenService.matches(sessionOpt.get(), parsedToken)) {
                        return Uni.createFrom().voidItem();
                    }

                    var session = sessionOpt.get();
                    if (session.revokedAt() != null) {
                        return Uni.createFrom().voidItem();
                    }

                    return authSessionTokenService.revoke(session, OffsetDateTime.now()).replaceWithVoid();
                });
    }
}
