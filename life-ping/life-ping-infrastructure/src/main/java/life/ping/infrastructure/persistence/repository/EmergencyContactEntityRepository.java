package life.ping.infrastructure.persistence.repository;

import io.smallrye.mutiny.Uni;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import life.ping.infrastructure.persistence.entity.EmergencyContactEntity;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class EmergencyContactEntityRepository implements PanacheRepositoryBase<EmergencyContactEntity, UUID> {
    public Uni<List<EmergencyContactEntity>> findByAccountId(UUID accountId) {
        return find("accountId", accountId).list();
    }

    public Uni<Integer> updateContact(UUID id,
                                      String name,
                                      String email,
                                      String language,
                                      java.time.LocalDateTime updatedAt) {
        return update(
                "name = ?1, email = ?2, language = ?3, updatedAt = ?4 where id = ?5",
                name,
                email,
                language,
                updatedAt,
                id
        );
    }
}
