package life.ping.infrastructure.persistence.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import life.ping.infrastructure.persistence.entity.MissedDayMarkerEntity;

import java.util.UUID;

@ApplicationScoped
public class MissedDayMarkerEntityRepository implements PanacheRepositoryBase<MissedDayMarkerEntity, UUID> {
}
