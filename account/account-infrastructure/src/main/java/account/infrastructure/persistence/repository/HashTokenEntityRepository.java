package account.infrastructure.persistence.repository;

import account.infrastructure.persistence.entity.HashTokenEntity;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class HashTokenEntityRepository implements PanacheRepositoryBase<HashTokenEntity, UUID> {
}
