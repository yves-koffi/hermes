package life.ping.infrastructure.persistence.repository;

import io.smallrye.mutiny.Uni;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import life.ping.infrastructure.persistence.entity.AccountEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class AccountEntityRepository implements PanacheRepositoryBase<AccountEntity, UUID> {
    public Uni<AccountEntity> findByAppUuid(String appUuid) {
        return find("appUuid", appUuid).firstResult();
    }

    public Uni<Integer> updateCheckinState(UUID id, LocalDateTime lastCheckinAt, LocalDateTime updatedAt, boolean resetMissedStreak) {
        if (resetMissedStreak) {
            return update("missedStreak = 0, lastCheckinAt = ?1, updatedAt = ?2 where id = ?3", lastCheckinAt, updatedAt, id);
        }
        return update("lastCheckinAt = ?1, updatedAt = ?2 where id = ?3", lastCheckinAt, updatedAt, id);
    }

    public Uni<Integer> updateUserSettings(UUID id,
                                           String userName,
                                           java.time.LocalTime callbackTime,
                                           Integer checkInFrequency,
                                           Integer thresholdPeriod,
                                           LocalDateTime updatedAt) {
        return update(
                "userName = ?1, callbackTime = ?2, checkInFrequency = ?3, thresholdPeriod = ?4, updatedAt = ?5 where id = ?6",
                userName,
                callbackTime,
                checkInFrequency,
                thresholdPeriod,
                updatedAt,
                id
        );
    }
}
