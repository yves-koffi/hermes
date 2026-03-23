package account.infrastructure.persistence.repository;

import account.infrastructure.persistence.entity.AuthSessionEntity;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class AuthSessionEntityRepository implements PanacheRepositoryBase<AuthSessionEntity, UUID> {
}
