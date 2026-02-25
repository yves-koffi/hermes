package business.card.infrastructure.persistence.repository;

import business.card.infrastructure.persistence.entity.BusinessCardEntity;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class BusinessCardEntityRepository implements PanacheRepositoryBase<BusinessCardEntity, UUID> {
}
