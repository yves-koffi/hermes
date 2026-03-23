package account.infrastructure.persistence.adapter;

import account.application.spi.HashTokenRepository;
import account.domain.model.HashToken;
import account.domain.model.TokenType;
import account.infrastructure.persistence.mapper.HashTokenMapper;
import account.infrastructure.persistence.repository.HashTokenEntityRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class HashTokenRepositoryImpl implements HashTokenRepository {

    @Inject
    HashTokenEntityRepository hashTokenEntityRepository;
    @Inject
    HashTokenMapper hashTokenMapper;

    @Override
    public Uni<HashToken> save(HashToken hashToken) {
        var entity = hashTokenMapper.toEntity(hashToken);
        return hashTokenEntityRepository.persistAndFlush(entity)
                .replaceWith(entity)
                .map(hashTokenMapper::toDomain);
    }

    @Override
    public Uni<Optional<HashToken>> findById(UUID id) {
        return hashTokenEntityRepository.findById(id)
                .map(entity -> Optional.ofNullable(hashTokenMapper.toDomain(entity)));
    }

    @Override
    public Uni<Optional<HashToken>> findByHashToken(String hashToken) {
        return hashTokenEntityRepository.find("hashToken", hashToken).firstResult()
                .map(entity -> Optional.ofNullable(hashTokenMapper.toDomain(entity)));
    }

    @Override
    public Uni<List<HashToken>> findByAccountId(UUID accountId) {
        return hashTokenEntityRepository.list("accountId", accountId)
                .map(entities -> entities.stream().map(hashTokenMapper::toDomain).toList());
    }

    @Override
    public Uni<Void> deleteById(UUID id) {
        return hashTokenEntityRepository.deleteById(id).replaceWithVoid();
    }

    @Override
    public Uni<Void> deleteByAccountIdAndTokenTypes(UUID accountId, List<TokenType> tokenTypes) {
        return hashTokenEntityRepository.delete("accountId = ?1 and tokenType in ?2", accountId, tokenTypes)
                .replaceWithVoid();
    }
}
