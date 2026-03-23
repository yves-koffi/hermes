package account.application.spi;

import account.domain.model.AuthSession;
import io.smallrye.mutiny.Uni;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface AuthSessionRepository {

    Uni<AuthSession> save(AuthSession authSession);

    Uni<Optional<AuthSession>> findById(UUID id);

    Uni<Optional<AuthSession>> findByRefreshTokenHash(String refreshTokenHash);

    Uni<Void> revokeAllByAccountId(UUID accountId, OffsetDateTime revokedAt);
}
