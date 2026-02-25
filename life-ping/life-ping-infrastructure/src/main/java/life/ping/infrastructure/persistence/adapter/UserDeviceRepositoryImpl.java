package life.ping.infrastructure.persistence.adapter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.smallrye.mutiny.Uni;
import life.ping.domain.model.UserDevice;
import life.ping.application.spi.UserDeviceRepository;
import life.ping.infrastructure.persistence.mapper.UserDeviceMapper;
import life.ping.infrastructure.persistence.repository.UserDeviceEntityRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserDeviceRepositoryImpl implements UserDeviceRepository {

    private final UserDeviceEntityRepository userDeviceEntityRepository;
    private final UserDeviceMapper userDeviceMapper;

    @Inject
    public UserDeviceRepositoryImpl(UserDeviceEntityRepository userDeviceEntityRepository, UserDeviceMapper userDeviceMapper) {
        this.userDeviceEntityRepository = userDeviceEntityRepository;
        this.userDeviceMapper = userDeviceMapper;
    }

    @Override
    public Uni<UserDevice> save(UserDevice userDevice) {
        var entity = userDeviceMapper.toEntity(userDevice);
        return userDeviceEntityRepository.persistAndFlush(entity)
                .replaceWith(entity)
                .map(userDeviceMapper::toDomain);
    }

    @Override
    public Uni<Optional<UserDevice>> findById(UUID id) {
        return userDeviceEntityRepository.findById(id)
                .map(entity -> Optional.ofNullable(userDeviceMapper.toDomain(entity)));
    }

    @Override
    public Uni<List<UserDevice>> findAll() {
        return userDeviceEntityRepository.listAll()
                .map(entities -> entities.stream().map(userDeviceMapper::toDomain).toList());
    }

    @Override
    public Uni<Void> deleteById(UUID id) {
        return userDeviceEntityRepository.deleteById(id).replaceWithVoid();
    }
}
