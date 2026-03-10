package account.application.spi;

import account.domain.model.HashToken;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HashTokenRepository {

    Uni<HashToken> save(HashToken hashToken);

    Uni<Optional<HashToken>> findById(UUID id);

    Uni<Optional<HashToken>> findByHashToken(String hashToken);

    Uni<List<HashToken>> findByAccountId(UUID accountId);

    Uni<Void> deleteById(UUID id);
}
