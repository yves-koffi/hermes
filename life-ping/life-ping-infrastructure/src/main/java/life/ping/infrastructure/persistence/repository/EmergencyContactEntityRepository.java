package life.ping.infrastructure.persistence.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import life.ping.infrastructure.persistence.entity.EmergencyContactEntity;

import java.util.UUID;

@ApplicationScoped
public class EmergencyContactEntityRepository implements PanacheRepositoryBase<EmergencyContactEntity, UUID> {
}
