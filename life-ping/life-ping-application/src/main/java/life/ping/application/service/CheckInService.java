package life.ping.application.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import life.ping.application.spi.AccountRepository;
import life.ping.application.spi.DailyCheckinRepository;
import life.ping.application.usecase.CheckInUseCase;
import life.ping.domain.model.CheckinSource;
import life.ping.domain.model.DailyCheckin;
import shared.domain.exception.DomainNotFoundException;
import shared.domain.exception.DomainValidationException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class CheckInService implements CheckInUseCase {
    private static final long MAX_FUTURE_DAYS = 2;

    @Inject
    AccountRepository accountRepository;
    @Inject
    DailyCheckinRepository dailyCheckinRepository;


    @Override
    public Uni<Output> handle(Input in) {
        validate(in);

        return Panache.withTransaction(() -> {
                    LocalDateTime nowUtc = LocalDateTime.now(Clock.systemUTC());
                    return accountRepository.findById(in.userId())
                            .flatMap(account -> account
                                    .map(foundAccount -> upsertCheckIn(in, foundAccount.timezone(), nowUtc))
                                    .orElseGet(() -> Uni.createFrom().failure(accountNotFound(in.userId()))));
                });
    }

    private Uni<Output> upsertCheckIn(Input in, String timezone, LocalDateTime nowUtc) {
        return dailyCheckinRepository.findByAccountIdAndLocalDate(in.userId(), in.localDate())
                .flatMap(existingCheckIn -> existingCheckIn
                        .map(checkIn -> touchAccount(in.userId(), timezone, in.localDate(), nowUtc)
                                .replaceWith(toOutput(checkIn, false)))
                        .orElseGet(() -> dailyCheckinRepository.save(new DailyCheckin(
                                        null,
                                        in.userId(),
                                        in.localDate(),
                                        nowUtc,
                                        CheckinSource.MOBILE.name()
                                ))
                                .flatMap(savedCheckIn -> touchAccount(in.userId(), timezone, in.localDate(), nowUtc)
                                        .replaceWith(toOutput(savedCheckIn, true)))));
    }

    private Uni<Void> touchAccount(UUID userId, String timezone, LocalDate checkinLocalDate, LocalDateTime nowUtc) {
        return accountRepository.updateCheckinState(
                        userId,
                        nowUtc,
                        nowUtc,
                        shouldResetMissedStreak(timezone, checkinLocalDate)
                )
                .flatMap(updatedRows -> updatedRows == 1
                        ? Uni.createFrom().voidItem()
                        : Uni.createFrom().failure(accountNotFound(userId)));
    }

    private Output toOutput(DailyCheckin checkIn, boolean created) {
        return new Output(
                checkIn.id(),
                created,
                checkIn.checkedInAt().toInstant(ZoneOffset.UTC)
        );
    }

    private void validate(Input in) {
        if (in == null) {
            throw DomainValidationException.requiredField("input");
        }
        if (in.localDate().isAfter(java.time.LocalDate.now(Clock.systemUTC()).plusDays(MAX_FUTURE_DAYS))) {
            throw DomainValidationException.invalidField(
                    "localDate",
                    in.localDate(),
                    "must not be more than " + MAX_FUTURE_DAYS + " day(s) in the future"
            );
        }
        if (in.source() != CheckinSource.MOBILE) {
            throw DomainValidationException.invalidField("source", in.source(), "MOBILE required");
        }
    }

    private DomainNotFoundException accountNotFound(UUID userId) {
        return DomainNotFoundException.of(
                "ACCOUNT_NOT_FOUND",
                "account.not_found",
                Map.of("userId", userId)
        );
    }

    private boolean shouldResetMissedStreak(String timezone, LocalDate checkinLocalDate) {
        return checkinLocalDate.equals(LocalDate.now(ZoneId.of(timezone)));
    }
}
