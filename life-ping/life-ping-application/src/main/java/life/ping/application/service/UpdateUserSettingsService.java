package life.ping.application.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import life.ping.application.spi.AccountRepository;
import life.ping.application.spi.EmergencyContactRepository;
import life.ping.application.usecase.UpdateUserSettingsUseCase;
import life.ping.domain.model.Account;
import life.ping.domain.model.EmergencyContact;
import shared.domain.exception.DomainNotFoundException;
import shared.domain.exception.DomainValidationException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class UpdateUserSettingsService implements UpdateUserSettingsUseCase {
    @Inject
    AccountRepository accountRepository;
    @Inject
    EmergencyContactRepository emergencyContactRepository;

    @Override
    public Uni<Void> handle(Input in) {
        validate(in);

        ParsedInput parsed = parse(in);
        LocalDateTime now = LocalDateTime.now(Clock.systemUTC());

        return Panache.withTransaction(() ->
                accountRepository.findByAppUuid(in.appUid())
                        .flatMap(account -> account
                                .map(foundAccount -> updateSettings(foundAccount, parsed, now))
                                .orElseGet(() -> Uni.createFrom().failure(accountNotFound(in.appUid()))))
        );
    }

    private Uni<Void> updateSettings(Account account, ParsedInput parsed, LocalDateTime now) {
        return accountRepository.updateUserSettings(
                        account.id(),
                        parsed.name(),
                        parsed.callbackTime(),
                        parsed.checkInFrequency(),
                        parsed.thresholdPeriod(),
                        now
                )
                .flatMap(updatedRows -> updatedRows == 1
                        ? upsertEmergencyContact(account.id(), parsed, now)
                        : Uni.createFrom().failure(accountNotFound(account.id())));
    }

    private Uni<Void> upsertEmergencyContact(UUID accountId, ParsedInput parsed, LocalDateTime now) {
        return emergencyContactRepository.findByAccountId(accountId)
                .flatMap(contacts -> {
                    EmergencyContact existing = contacts.isEmpty() ? null : contacts.getFirst();
                    if (existing == null) {
                        return createEmergencyContact(accountId, parsed, now);
                    }

                    return emergencyContactRepository.updateContact(new EmergencyContact(
                                    existing.id(),
                                    existing.accountId(),
                                    nextValue(parsed.emergencyContactName(), existing.name()),
                                    requiredValue(parsed.emergencyContactEmail(), existing.email(), "emergencyContactEmail"),
                                    requiredValue(parsed.notificationLanguage(), existing.language(), "notificationLanguage"),
                                    existing.createdAt(),
                                    now
                            ))
                            .flatMap(updatedRows -> updatedRows == 1
                                    ? Uni.createFrom().voidItem()
                                    : Uni.createFrom().failure(contactNotFound(existing.id())));
                });
    }

    private Uni<Void> createEmergencyContact(UUID accountId, ParsedInput parsed, LocalDateTime now) {
        String email = requiredValue(parsed.emergencyContactEmail(), null, "emergencyContactEmail");
        String language = requiredValue(parsed.notificationLanguage(), null, "notificationLanguage");

        return emergencyContactRepository.save(new EmergencyContact(
                        null,
                        accountId,
                        parsed.emergencyContactName(),
                        email,
                        language,
                        now,
                        now
                ))
                .replaceWithVoid();
    }

    private ParsedInput parse(Input in) {
        return new ParsedInput(
                emptyToNull(in.name()),
                requireCallbackTime(in.callbackTime()),
                requirePositiveInteger("checkInFrequency", in.checkInFrequency()),
                requirePositiveInteger("thresholdPeriod", in.thresholdPeriod()),
                emptyToNull(in.emergencyContactName()),
                emptyToNull(in.emergencyContactEmail()),
                emptyToNull(in.notificationLanguage())
        );
    }

    private void validate(Input in) {
        if (in == null) {
            throw DomainValidationException.requiredField("input");
        }
        if (emptyToNull(in.appUid()) == null) {
            throw DomainValidationException.requiredField("appUid");
        }
    }

    private LocalTime requireCallbackTime(LocalTime value) {
        if (value == null) {
            throw DomainValidationException.requiredField("callbackTime");
        }
        return value;
    }

    private Integer requirePositiveInteger(String field, Integer value) {
        if (value == null) {
            throw DomainValidationException.requiredField(field);
        }
        if (value <= 0) {
            throw DomainValidationException.invalidField(field, value, "must be greater than 0");
        }
        return value;
    }

    private String requiredValue(String candidate, String fallback, String field) {
        String value = nextValue(candidate, fallback);
        if (value == null) {
            throw DomainValidationException.requiredField(field);
        }
        return value;
    }

    private String nextValue(String candidate, String fallback) {
        return candidate != null ? candidate : fallback;
    }

    private String emptyToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private DomainNotFoundException accountNotFound(Object appUid) {
        return DomainNotFoundException.of(
                "ACCOUNT_NOT_FOUND",
                "account.not_found",
                Map.of("appUid", appUid)
        );
    }

    private DomainNotFoundException contactNotFound(UUID contactId) {
        return DomainNotFoundException.of(
                "EMERGENCY_CONTACT_NOT_FOUND",
                "emergency_contact.not_found",
                Map.of("contactId", contactId)
        );
    }

    private record ParsedInput(
            String name,
            LocalTime callbackTime,
            Integer checkInFrequency,
            Integer thresholdPeriod,
            String emergencyContactName,
            String emergencyContactEmail,
            String notificationLanguage
    ) {
    }
}
