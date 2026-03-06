package store.purchase.infrastructure.persistence.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import store.purchase.infrastructure.persistence.entity.PurchaseEntity;

import java.util.UUID;

@ApplicationScoped
public class PurchaseEntityRepository implements PanacheRepositoryBase<PurchaseEntity, UUID> {
}
