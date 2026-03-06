package life.ping.infrastructure.persistence.adapter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.smallrye.mutiny.Uni;
import life.ping.domain.model.DailyCheckin;
import life.ping.application.spi.DailyCheckinRepository;
import life.ping.infrastructure.persistence.mapper.DailyCheckinMapper;
import life.ping.infrastructure.persistence.repository.DailyCheckinEntityRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class DailyCheckinRepositoryImpl implements DailyCheckinRepository {

    private final DailyCheckinEntityRepository dailyCheckinEntityRepository;
    private final DailyCheckinMapper dailyCheckinMapper;

    @Inject
    public DailyCheckinRepositoryImpl(DailyCheckinEntityRepository dailyCheckinEntityRepository, DailyCheckinMapper dailyCheckinMapper) {
        this.dailyCheckinEntityRepository = dailyCheckinEntityRepository;
        this.dailyCheckinMapper = dailyCheckinMapper;
    }

    @Override
    public Uni<DailyCheckin> save(DailyCheckin dailyCheckin) {
        var entity = dailyCheckinMapper.toEntity(dailyCheckin);
        return dailyCheckinEntityRepository.persistAndFlush(entity)
                .replaceWith(entity)
                .map(dailyCheckinMapper::toDomain);
    }

    @Override
    public Uni<Boolean> saveIfAbsent(DailyCheckin dailyCheckin) {
        var entity = dailyCheckinMapper.toEntity(dailyCheckin);
        return dailyCheckinEntityRepository.persistAndFlush(entity)
                .replaceWith(Boolean.TRUE)
                .onFailure(this::isDuplicateCheckinViolation)
                .recoverWithItem(Boolean.FALSE);
    }

    @Override
    public Uni<Optional<DailyCheckin>> findById(UUID id) {
        return dailyCheckinEntityRepository.findById(id)
                .map(entity -> Optional.ofNullable(dailyCheckinMapper.toDomain(entity)));
    }

    @Override
    public Uni<Optional<DailyCheckin>> findByAccountIdAndLocalDate(UUID accountId, LocalDate localDate) {
        return dailyCheckinEntityRepository.findByAccountIdAndLocalDate(accountId, localDate)
                .map(entity -> Optional.ofNullable(dailyCheckinMapper.toDomain(entity)));
    }

    @Override
    public Uni<List<DailyCheckin>> findAll() {
        return dailyCheckinEntityRepository.listAll()
                .map(entities -> entities.stream().map(dailyCheckinMapper::toDomain).toList());
    }

    @Override
    public Uni<Void> deleteById(UUID id) {
        return dailyCheckinEntityRepository.deleteById(id).replaceWithVoid();
    }

    private boolean isDuplicateCheckinViolation(Throwable failure) {
        for (Throwable current = failure; current != null; current = current.getCause()) {
            String message = current.getMessage();
            if (message != null && (message.contains("uk_daily_checkins_account_id_local_date")
                    || message.contains("uk_daily_checkins_user_id_local_date")
                    || message.contains("duplicate key value")
                    || message.contains("SQLSTATE 23505"))) {
                return true;
            }
        }
        return false;
    }
}
