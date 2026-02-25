package life.ping.application.spi;

import io.smallrye.mutiny.Uni;
import life.ping.domain.model.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    Uni<Account> save(Account account);

    Uni<Optional<Account>> findById(UUID id);

    Uni<List<Account>> findAll();

    Uni<Void> deleteById(UUID id);
}
