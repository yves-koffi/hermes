package life.ping.application.spi;

import io.smallrye.mutiny.Uni;
import life.ping.domain.model.EmergencyContact;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmergencyContactRepository {
    Uni<EmergencyContact> save(EmergencyContact emergencyContact);

    Uni<Optional<EmergencyContact>> findById(UUID id);

    Uni<List<EmergencyContact>> findAll();

    Uni<Void> deleteById(UUID id);
}
