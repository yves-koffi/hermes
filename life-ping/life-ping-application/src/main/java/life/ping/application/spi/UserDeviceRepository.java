package life.ping.application.spi;

import io.smallrye.mutiny.Uni;
import life.ping.domain.model.UserDevice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDeviceRepository {
    Uni<UserDevice> save(UserDevice userDevice);

    Uni<Optional<UserDevice>> findById(UUID id);

    Uni<List<UserDevice>> findAll();

    Uni<Void> deleteById(UUID id);
}
