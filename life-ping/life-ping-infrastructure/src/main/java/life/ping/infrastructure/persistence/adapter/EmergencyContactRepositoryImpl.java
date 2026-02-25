package life.ping.infrastructure.persistence.adapter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.smallrye.mutiny.Uni;
import life.ping.domain.model.EmergencyContact;
import life.ping.application.spi.EmergencyContactRepository;
import life.ping.infrastructure.persistence.mapper.EmergencyContactMapper;
import life.ping.infrastructure.persistence.repository.EmergencyContactEntityRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class EmergencyContactRepositoryImpl implements EmergencyContactRepository {

    private final EmergencyContactEntityRepository emergencyContactEntityRepository;
    private final EmergencyContactMapper emergencyContactMapper;

    @Inject
    public EmergencyContactRepositoryImpl(EmergencyContactEntityRepository emergencyContactEntityRepository, EmergencyContactMapper emergencyContactMapper) {
        this.emergencyContactEntityRepository = emergencyContactEntityRepository;
        this.emergencyContactMapper = emergencyContactMapper;
    }

    @Override
    public Uni<EmergencyContact> save(EmergencyContact emergencyContact) {
        var entity = emergencyContactMapper.toEntity(emergencyContact);
        return emergencyContactEntityRepository.persistAndFlush(entity)
                .replaceWith(entity)
                .map(emergencyContactMapper::toDomain);
    }

    @Override
    public Uni<Optional<EmergencyContact>> findById(UUID id) {
        return emergencyContactEntityRepository.findById(id)
                .map(entity -> Optional.ofNullable(emergencyContactMapper.toDomain(entity)));
    }

    @Override
    public Uni<List<EmergencyContact>> findAll() {
        return emergencyContactEntityRepository.listAll()
                .map(entities -> entities.stream().map(emergencyContactMapper::toDomain).toList());
    }

    @Override
    public Uni<Void> deleteById(UUID id) {
        return emergencyContactEntityRepository.deleteById(id).replaceWithVoid();
    }
}
