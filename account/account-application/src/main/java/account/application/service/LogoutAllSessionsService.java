package account.application.service;

import account.application.spi.AuthSessionRepository;
import account.application.usecase.LogoutAllSessionsUseCase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import shared.application.context.RequestContext;
import shared.domain.exception.AuthenticationException;

import java.time.OffsetDateTime;

/**
 * Implémentation du use case de déconnexion globale.
 *
 * Le service s'appuie sur le compte présent dans le contexte de requête et révoque
 * toutes les sessions persistées encore actives. Il est conçu pour les cas de sécurité
 * renforcée ou pour offrir une action "déconnecter tous mes appareils".
 */
@ApplicationScoped
public class LogoutAllSessionsService implements LogoutAllSessionsUseCase {

    @Inject
    RequestContext context;
    @Inject
    AuthSessionRepository authSessionRepository;

    @Override
    public Uni<Void> execute() {
        if (context.getExecutionContext() == null || context.getExecutionContext().accountId() == null) {
            return Uni.createFrom().failure(AuthenticationException.invalidCredentials());
        }
        return authSessionRepository.revokeAllByAccountId(
                context.getExecutionContext().accountId(),
                OffsetDateTime.now()
        );
    }
}
