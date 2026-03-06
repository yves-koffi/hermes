package life.ping.application.spi;

import io.smallrye.mutiny.Uni;
import life.ping.domain.model.UserDevice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDeviceRepository {
    Uni<UserDevice> save(UserDevice userDevice);

    Uni<Optional<UserDevice>> findById(UUID id);

    Uni<Optional<UserDevice>> findByAccountIdAndFcmToken(UUID accountId, String fcmToken);

    Uni<Integer> updateRegistration(UUID id, String platform, LocalDateTime lastSeenAt, LocalDateTime softDeletedAt);

    Uni<List<UserDevice>> findAll();

    Uni<Void> deleteById(UUID id);
}
