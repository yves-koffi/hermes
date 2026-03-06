package life.ping.application.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import life.ping.application.spi.AccountRepository;
import life.ping.application.spi.EmergencyContactRepository;
import life.ping.application.spi.EmergencyEmailSender;
import life.ping.application.usecase.SendSOSRequestUseCase;
import shared.domain.exception.DomainNotFoundException;
import shared.domain.exception.DomainValidationException;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class SendSOSRequestService implements SendSOSRequestUseCase {
    @Inject
    AccountRepository accountRepository;
    @Inject
    EmergencyContactRepository emergencyContactRepository;
    @Inject
    EmergencyEmailSender emergencyEmailSender;

    @Override
    public Uni<Output> handle(Input in) {
        validate(in);

        Instant requestedAt = in.localDate() == null ? Instant.now(Clock.systemUTC()) : in.localDate();

        return accountRepository.findById(in.userId())
                .flatMap(account -> account
                        .map(foundAccount -> emergencyContactRepository.findByAccountId(foundAccount.id())
                                .flatMap(contacts -> {
                                    if (contacts.isEmpty()) {
                                        return Uni.createFrom().failure(noEmergencyContact(foundAccount.id()));
                                    }
                                    return emergencyEmailSender.sendSosAlert(foundAccount, contacts, requestedAt)
                                            .replaceWith(new Output(Instant.now(Clock.systemUTC())));
                                }))
                        .orElseGet(() -> Uni.createFrom().failure(accountNotFound(in.userId()))));
    }

    private void validate(Input in) {
        if (in == null) {
            throw DomainValidationException.requiredField("input");
        }
        if (in.userId() == null) {
            throw DomainValidationException.requiredField("userId");
        }
    }

    private DomainNotFoundException accountNotFound(UUID userId) {
        return DomainNotFoundException.of(
                "ACCOUNT_NOT_FOUND",
                "account.not_found",
                Map.of("userId", userId)
        );
    }

    private DomainValidationException noEmergencyContact(UUID userId) {
        return DomainValidationException.of(
                "EMERGENCY_CONTACT_REQUIRED",
                "emergency_contact.required",
                Map.of("userId", userId)
        );
    }
}
