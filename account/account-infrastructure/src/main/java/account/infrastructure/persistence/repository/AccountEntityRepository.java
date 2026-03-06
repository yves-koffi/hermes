package account.infrastructure.persistence.repository;

import account.infrastructure.persistence.entity.AccountEntity;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class AccountEntityRepository implements PanacheRepositoryBase<AccountEntity, UUID> {
}
