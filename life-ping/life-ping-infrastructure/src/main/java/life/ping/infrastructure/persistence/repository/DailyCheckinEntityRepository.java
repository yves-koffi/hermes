package life.ping.infrastructure.persistence.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import life.ping.infrastructure.persistence.entity.DailyCheckinEntity;

import java.util.UUID;

@ApplicationScoped
public class DailyCheckinEntityRepository implements PanacheRepositoryBase<DailyCheckinEntity, UUID> {
}
