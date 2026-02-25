package life.ping.infrastructure.persistence.adapter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.smallrye.mutiny.Uni;
import life.ping.domain.model.MissedDayMarker;
import life.ping.application.spi.MissedDayMarkerRepository;
import life.ping.infrastructure.persistence.mapper.MissedDayMarkerMapper;
import life.ping.infrastructure.persistence.repository.MissedDayMarkerEntityRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class MissedDayMarkerRepositoryImpl implements MissedDayMarkerRepository {

    private final MissedDayMarkerEntityRepository missedDayMarkerEntityRepository;
    private final MissedDayMarkerMapper missedDayMarkerMapper;

    @Inject
    public MissedDayMarkerRepositoryImpl(MissedDayMarkerEntityRepository missedDayMarkerEntityRepository, MissedDayMarkerMapper missedDayMarkerMapper) {
        this.missedDayMarkerEntityRepository = missedDayMarkerEntityRepository;
        this.missedDayMarkerMapper = missedDayMarkerMapper;
    }

    @Override
    public Uni<MissedDayMarker> save(MissedDayMarker missedDayMarker) {
        var entity = missedDayMarkerMapper.toEntity(missedDayMarker);
        return missedDayMarkerEntityRepository.persistAndFlush(entity)
                .replaceWith(entity)
                .map(missedDayMarkerMapper::toDomain);
    }

    @Override
    public Uni<Optional<MissedDayMarker>> findById(UUID id) {
        return missedDayMarkerEntityRepository.findById(id)
                .map(entity -> Optional.ofNullable(missedDayMarkerMapper.toDomain(entity)));
    }

    @Override
    public Uni<List<MissedDayMarker>> findAll() {
        return missedDayMarkerEntityRepository.listAll()
                .map(entities -> entities.stream().map(missedDayMarkerMapper::toDomain).toList());
    }

    @Override
    public Uni<Void> deleteById(UUID id) {
        return missedDayMarkerEntityRepository.deleteById(id).replaceWithVoid();
    }
}
