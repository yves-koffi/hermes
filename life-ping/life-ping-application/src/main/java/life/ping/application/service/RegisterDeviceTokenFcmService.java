package life.ping.application.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import life.ping.application.spi.AccountRepository;
import life.ping.application.spi.UserDeviceRepository;
import life.ping.application.usecase.RegisterDeviceTokenFcmUseCase;
import life.ping.domain.model.UserDevice;
import shared.domain.exception.DomainNotFoundException;
import shared.domain.exception.DomainValidationException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class RegisterDeviceTokenFcmService implements RegisterDeviceTokenFcmUseCase {
    @Inject
    AccountRepository accountRepository;
    @Inject
    UserDeviceRepository userDeviceRepository;

    @Override
    public Uni<Void> handle(Input in) {
        validate(in);

        LocalDateTime seenAt = in.seenAt() == null
                ? LocalDateTime.now(Clock.systemUTC())
                : LocalDateTime.ofInstant(in.seenAt(), ZoneOffset.UTC);

        return Panache.withTransaction(() ->
                accountRepository.findByAppUuid(in.appUid().toString())
                        .flatMap(account -> account
                                .map(foundAccount -> userDeviceRepository.findByAccountIdAndFcmToken(foundAccount.id(), in.fcmToken())
                                        .flatMap(existingDevice -> existingDevice
                                                .map(device -> userDeviceRepository.updateRegistration(
                                                                device.id(),
                                                                in.platform(),
                                                                seenAt,
                                                                null
                                                        )
                                                        .replaceWithVoid())
                                                .orElseGet(() -> userDeviceRepository.save(new UserDevice(
                                                                        null,
                                                                        foundAccount.id(),
                                                                        in.platform(),
                                                                        in.fcmToken(),
                                                                        seenAt,
                                                                        seenAt,
                                                                        null
                                                                ))
                                                                .replaceWithVoid())))
                                .orElseGet(() -> Uni.createFrom().failure(accountNotFound(in.appUid()))))
        );
    }

    private void validate(Input in) {
        if (in == null) {
            throw DomainValidationException.requiredField("input");
        }
        if (in.appUid() == null) {
            throw DomainValidationException.requiredField("appUid");
        }
        if (in.platform() == null || in.platform().isBlank()) {
            throw DomainValidationException.requiredField("platform");
        }
        if (in.fcmToken() == null || in.fcmToken().isBlank()) {
            throw DomainValidationException.requiredField("fcmToken");
        }
    }

    private DomainNotFoundException accountNotFound(UUID appUid) {
        return DomainNotFoundException.of(
                "ACCOUNT_NOT_FOUND",
                "account.not_found",
                Map.of("appUid", appUid)
        );
    }
}
