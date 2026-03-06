package life.ping.application.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import life.ping.application.spi.AccountRepository;
import life.ping.application.spi.DailyCheckinRepository;
import life.ping.application.usecase.SyncCheckInsUseCase;
import life.ping.domain.model.Account;
import life.ping.domain.model.CheckinSource;
import life.ping.domain.model.DailyCheckin;
import shared.domain.exception.DomainNotFoundException;
import shared.domain.exception.DomainValidationException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class SyncCheckInsService implements SyncCheckInsUseCase {
    @Inject
    AccountRepository accountRepository;
    @Inject
    DailyCheckinRepository dailyCheckinRepository;

    @Override
    public Uni<Output> handle(Input in) {
        validate(in);

        return Panache.withTransaction(() ->
                accountRepository.findById(in.userId())
                        .flatMap(account -> account
                                .map(foundAccount -> syncCheckIns(foundAccount, in.items()))
                                .orElseGet(() -> Uni.createFrom().failure(accountNotFound(in.userId()))))
        );
    }

    private Uni<Output> syncCheckIns(Account account, List<Item> items) {
        LocalDateTime nowUtc = LocalDateTime.now(Clock.systemUTC());
        List<NormalizedItem> normalizedItems = normalize(items, nowUtc);
        boolean resetMissedStreak = containsTodayCheckIn(account.timezone(), normalizedItems);
        LocalDateTime lastCheckinAt = latestCheckedInAt(normalizedItems, nowUtc);

        return Multi.createFrom().iterable(normalizedItems)
                .onItem().transformToUniAndConcatenate(item -> upsertCheckIn(account.id(), item))
                .collect().asList()
                .flatMap(results -> accountRepository.updateCheckinState(account.id(), lastCheckinAt, nowUtc, resetMissedStreak)
                        .flatMap(updatedRows -> updatedRows == 1
                                ? Uni.createFrom().item(toOutput(results, nowUtc))
                                : Uni.createFrom().failure(accountNotFound(account.id()))));
    }

    private Uni<Boolean> upsertCheckIn(UUID userId, NormalizedItem item) {
        return dailyCheckinRepository.findByAccountIdAndLocalDate(userId, item.localDate())
                .flatMap(existingCheckIn -> existingCheckIn.isPresent()
                        ? Uni.createFrom().item(Boolean.FALSE)
                        : dailyCheckinRepository.saveIfAbsent(new DailyCheckin(
                                null,
                                userId,
                                item.localDate(),
                                item.checkedInAt(),
                                CheckinSource.SYNC.name()
                        )));
    }

    private List<NormalizedItem> normalize(List<Item> items, LocalDateTime nowUtc) {
        return items.stream()
                .map(item -> new NormalizedItem(
                        item.localDate(),
                        item.checkedInAt() == null ? nowUtc : LocalDateTime.ofInstant(item.checkedInAt(), ZoneOffset.UTC)
                ))
                .toList();
    }

    private LocalDateTime latestCheckedInAt(List<NormalizedItem> items, LocalDateTime nowUtc) {
        return items.stream()
                .map(NormalizedItem::checkedInAt)
                .max(LocalDateTime::compareTo).filter(candidate -> candidate.isAfter(nowUtc)).orElse(nowUtc);
    }

    private boolean containsTodayCheckIn(String timezone, List<NormalizedItem> items) {
        LocalDate todayLocalDate = LocalDate.now(ZoneId.of(timezone));
        return items.stream().anyMatch(item -> item.localDate().equals(todayLocalDate));
    }

    private Output toOutput(List<Boolean> results, LocalDateTime nowUtc) {
        int inserted = (int) results.stream().filter(Boolean.TRUE::equals).count();
        return new Output(
                results.size(),
                inserted,
                results.size() - inserted,
                nowUtc.toInstant(ZoneOffset.UTC)
        );
    }

    private void validate(Input in) {
        if (in == null) {
            throw DomainValidationException.requiredField("input");
        }
        if (in.userId() == null) {
            throw DomainValidationException.requiredField("userId");
        }
        if (in.items() == null || in.items().isEmpty()) {
            throw DomainValidationException.invalidField("items", in.items(), "must contain at least one item");
        }

        for (int i = 0; i < in.items().size(); i++) {
            Item item = in.items().get(i);
            if (item == null) {
                throw DomainValidationException.invalidField("items[" + i + "]", null, "must not be null");
            }
            if (item.localDate() == null) {
                throw DomainValidationException.requiredField("items[" + i + "].localDate");
            }
        }
    }

    private DomainNotFoundException accountNotFound(UUID userId) {
        return DomainNotFoundException.of(
                "ACCOUNT_NOT_FOUND",
                "account.not_found",
                Map.of("userId", userId)
        );
    }

    private record NormalizedItem(
            LocalDate localDate,
            LocalDateTime checkedInAt
    ) {
    }
}
