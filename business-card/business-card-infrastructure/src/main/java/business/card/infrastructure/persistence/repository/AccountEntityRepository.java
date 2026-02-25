package business.card.infrastructure.persistence.repository;

import business.card.infrastructure.persistence.entity.AccountEntity;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class AccountEntityRepository implements PanacheRepositoryBase<AccountEntity, UUID> {
}
