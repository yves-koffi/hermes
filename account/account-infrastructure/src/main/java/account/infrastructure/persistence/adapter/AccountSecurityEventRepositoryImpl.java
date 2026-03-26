package account.infrastructure.persistence.adapter;

import account.application.spi.AccountSecurityEventRepository;
import account.domain.model.AccountSecurityEvent;
import account.infrastructure.persistence.mapper.AccountSecurityEventMapper;
import account.infrastructure.persistence.repository.AccountSecurityEventEntityRepository;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AccountSecurityEventRepositoryImpl implements AccountSecurityEventRepository {

    @Inject
    AccountSecurityEventEntityRepository accountSecurityEventEntityRepository;
    @Inject
    AccountSecurityEventMapper accountSecurityEventMapper;

    @Override
    @WithTransaction("account")
    public Uni<AccountSecurityEvent> save(AccountSecurityEvent event) {
        var entity = accountSecurityEventMapper.toEntity(event);
        return accountSecurityEventEntityRepository.persistAndFlush(entity)
                .replaceWith(entity)
                .map(accountSecurityEventMapper::toDomain);
    }

    @Override
    @WithSession("account")
    public Uni<List<AccountSecurityEvent>> findRecentByAccountId(UUID accountId, int limit) {
        return accountSecurityEventEntityRepository.find("accountId = ?1 order by occurredAt desc", accountId)
                .page(0, limit)
                .list()
                .map(entities -> entities.stream().map(accountSecurityEventMapper::toDomain).toList());
    }
}
