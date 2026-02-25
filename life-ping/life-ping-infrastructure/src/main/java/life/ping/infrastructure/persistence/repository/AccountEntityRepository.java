package life.ping.infrastructure.persistence.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import life.ping.infrastructure.persistence.entity.AccountEntity;

import java.util.UUID;

@ApplicationScoped
public class AccountEntityRepository implements PanacheRepositoryBase<AccountEntity, UUID> {
}
