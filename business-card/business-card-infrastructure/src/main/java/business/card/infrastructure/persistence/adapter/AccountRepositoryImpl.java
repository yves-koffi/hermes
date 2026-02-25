package business.card.infrastructure.persistence.adapter;

import business.card.domain.model.Account;
import business.card.application.spi.AccountRepository;
import business.card.infrastructure.persistence.mapper.AccountMapper;
import business.card.infrastructure.persistence.repository.AccountEntityRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AccountRepositoryImpl implements AccountRepository {

    @Inject
    AccountEntityRepository accountEntityRepository;
    @Inject
    AccountMapper accountMapper;

    @Override
    public Uni<Account> save(Account account) {
        var entity = accountMapper.toEntity(account);
        return accountEntityRepository.persistAndFlush(entity)
                .replaceWith(entity)
                .map(accountMapper::toDomain);
    }

    @Override
    public Uni<Optional<Account>> findById(UUID id) {
        return accountEntityRepository.findById(id)
                .map(entity -> Optional.ofNullable(accountMapper.toDomain(entity)));
    }

    @Override
    public Uni<Optional<Account>> findByEmail(String email) {
        return accountEntityRepository.find("email", email).firstResult()
                .map(entity -> Optional.ofNullable(accountMapper.toDomain(entity)));
    }

    @Override
    public Uni<List<Account>> findAll() {
        return accountEntityRepository.listAll()
                .map(entities -> entities.stream().map(accountMapper::toDomain).toList());
    }

    @Override
    public Uni<Void> deleteById(UUID id) {
        return accountEntityRepository.deleteById(id).replaceWithVoid();
    }
}
