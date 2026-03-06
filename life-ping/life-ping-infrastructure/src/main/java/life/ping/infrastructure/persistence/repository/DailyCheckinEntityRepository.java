package life.ping.infrastructure.persistence.repository;

import io.smallrye.mutiny.Uni;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import life.ping.infrastructure.persistence.entity.DailyCheckinEntity;

import java.time.LocalDate;
import java.util.UUID;

@ApplicationScoped
public class DailyCheckinEntityRepository implements PanacheRepositoryBase<DailyCheckinEntity, UUID> {
    public Uni<DailyCheckinEntity> findByAccountIdAndLocalDate(UUID accountId, LocalDate localDate) {
        return find("accountId = ?1 and localDate = ?2", accountId, localDate).firstResult();
    }
}
