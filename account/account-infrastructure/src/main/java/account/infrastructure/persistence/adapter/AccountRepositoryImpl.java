package account.infrastructure.persistence.adapter;

import account.application.spi.AccountRepository;
import account.domain.model.Account;
import account.infrastructure.persistence.mapper.AccountMapper;
import account.infrastructure.persistence.repository.AccountEntityRepository;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import shared.domain.exception.DomainConflictException;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AccountRepositoryImpl implements AccountRepository {

    @Inject
    AccountEntityRepository accountEntityRepository;
    @Inject
    AccountMapper accountMapper;

    @Override
    @WithTransaction("account")
    public Uni<Account> save(Account account) {
        var entity = accountMapper.toEntity(account);
        return accountEntityRepository.getSession()
                .flatMap(session -> session.merge(entity)
                        .flatMap(merged -> session.flush().replaceWith(merged)))
                .onFailure(this::isDuplicateEmailViolation)
                .transform(failure -> new DomainConflictException(
                        "ACCOUNT_EMAIL_ALREADY_EXISTS",
                        "account.email.already_exists",
                        Map.of("email", account.email())
                ))
                .map(accountMapper::toDomain);
    }

    @Override
    @WithSession("account")
    public Uni<Optional<Account>> findById(UUID id) {
        return accountEntityRepository.findById(id)
                .map(entity -> Optional.ofNullable(accountMapper.toDomain(entity)));
    }

    @Override
    @WithSession("account")
    public Uni<Optional<Account>> findByEmail(String email) {
        String normalizedEmail = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
        return accountEntityRepository.find("lower(email) = ?1", normalizedEmail).firstResult()
                .map(entity -> Optional.ofNullable(accountMapper.toDomain(entity)));
    }

    @Override
    @WithSession("account")
    public Uni<List<Account>> findAll() {
        return accountEntityRepository.listAll()
                .map(entities -> entities.stream().map(accountMapper::toDomain).toList());
    }

    @Override
    @WithTransaction("account")
    public Uni<Void> deleteById(UUID id) {
        return accountEntityRepository.deleteById(id).replaceWithVoid();
    }

    private boolean isDuplicateEmailViolation(Throwable failure) {
        for (Throwable current = failure; current != null; current = current.getCause()) {
            String message = current.getMessage();
            if (message != null && (message.contains("uk_accounts_email_lower")
                    || message.contains("accounts_email_key")
                    || message.contains("duplicate key value")
                    || message.contains("SQLSTATE 23505"))) {
                return true;
            }
        }
        return false;
    }
}
