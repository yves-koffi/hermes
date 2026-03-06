package life.ping.infrastructure.persistence.adapter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.smallrye.mutiny.Uni;
import life.ping.domain.model.Account;
import life.ping.application.spi.AccountRepository;
import life.ping.infrastructure.persistence.mapper.AccountMapper;
import life.ping.infrastructure.persistence.repository.AccountEntityRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AccountRepositoryImpl implements AccountRepository {

    private final AccountEntityRepository accountEntityRepository;
    private final AccountMapper accountMapper;

    @Inject
    public AccountRepositoryImpl(AccountEntityRepository accountEntityRepository, AccountMapper accountMapper) {
        this.accountEntityRepository = accountEntityRepository;
        this.accountMapper = accountMapper;
    }

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
    public Uni<Optional<Account>> findByAppUuid(String appUuid) {
        return accountEntityRepository.findByAppUuid(appUuid)
                .map(entity -> Optional.ofNullable(accountMapper.toDomain(entity)));
    }

    @Override
    public Uni<Integer> updateCheckinState(UUID id, LocalDateTime lastCheckinAt, LocalDateTime updatedAt, boolean resetMissedStreak) {
        return accountEntityRepository.updateCheckinState(id, lastCheckinAt, updatedAt, resetMissedStreak);
    }

    @Override
    public Uni<Integer> updateUserSettings(UUID id, String userName, java.time.LocalTime callbackTime, Integer checkInFrequency, Integer thresholdPeriod, LocalDateTime updatedAt) {
        return accountEntityRepository.updateUserSettings(id, userName, callbackTime, checkInFrequency, thresholdPeriod, updatedAt);
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
