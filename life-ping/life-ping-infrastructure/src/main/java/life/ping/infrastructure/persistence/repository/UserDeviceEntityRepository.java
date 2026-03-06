package life.ping.infrastructure.persistence.repository;

import io.smallrye.mutiny.Uni;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import life.ping.infrastructure.persistence.entity.UserDeviceEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class UserDeviceEntityRepository implements PanacheRepositoryBase<UserDeviceEntity, UUID> {
    public Uni<UserDeviceEntity> findByAccountIdAndFcmToken(UUID accountId, String fcmToken) {
        return find("accountId = ?1 and fcmToken = ?2", accountId, fcmToken).firstResult();
    }

    public Uni<Integer> updateRegistration(UUID id, String platform, LocalDateTime lastSeenAt, LocalDateTime softDeletedAt) {
        return update(
                "platform = ?1, lastSeenAt = ?2, softDeletedAt = ?3 where id = ?4",
                platform,
                lastSeenAt,
                softDeletedAt,
                id
        );
    }
}
