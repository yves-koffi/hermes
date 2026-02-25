package business.card.application.spi;

import business.card.domain.model.Account;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

    Uni<Account> save(Account account);

    Uni<Optional<Account>> findById(UUID id);

    Uni<Optional<Account>> findByEmail(String email);

    Uni<List<Account>> findAll();

    Uni<Void> deleteById(UUID id);
}
