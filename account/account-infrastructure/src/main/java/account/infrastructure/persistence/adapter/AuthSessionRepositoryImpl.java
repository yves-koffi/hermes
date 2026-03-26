package account.infrastructure.persistence.adapter;

import account.application.spi.AuthSessionRepository;
import account.domain.model.AuthSession;
import account.infrastructure.persistence.mapper.AuthSessionMapper;
import account.infrastructure.persistence.repository.AuthSessionEntityRepository;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AuthSessionRepositoryImpl implements AuthSessionRepository {

    @Inject
    AuthSessionEntityRepository authSessionEntityRepository;
    @Inject
    AuthSessionMapper authSessionMapper;

    @Override
    @WithTransaction("account")
    public Uni<AuthSession> save(AuthSession authSession) {
        var entity = authSessionMapper.toEntity(authSession);
        return authSessionEntityRepository.getSession()
                .flatMap(session -> session.merge(entity)
                        .flatMap(merged -> session.flush().replaceWith(merged)))
                .map(authSessionMapper::toDomain);
    }

    @Override
    @WithSession("account")
    public Uni<Optional<AuthSession>> findById(UUID id) {
        return authSessionEntityRepository.findById(id)
                .map(entity -> Optional.ofNullable(authSessionMapper.toDomain(entity)));
    }

    @Override
    @WithSession("account")
    public Uni<Optional<AuthSession>> findByRefreshTokenHash(String refreshTokenHash) {
        return authSessionEntityRepository.find("refreshTokenHash", refreshTokenHash).firstResult()
                .map(entity -> Optional.ofNullable(authSessionMapper.toDomain(entity)));
    }

    @Override
    @WithTransaction("account")
    public Uni<Void> revokeAllByAccountId(UUID accountId, OffsetDateTime revokedAt) {
        return authSessionEntityRepository.update(
                        "revokedAt = ?1 where accountId = ?2 and revokedAt is null",
                        revokedAt,
                        accountId
                )
                .replaceWithVoid();
    }
}
