package life.ping.application.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import life.ping.application.spi.AccountRepository;
import life.ping.application.usecase.ConnectorUseCase;
import life.ping.domain.model.Account;
import shared.application.spi.JwtTokenProvider;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class ConnectorService implements ConnectorUseCase {
    private static final int DEFAULT_CHECK_IN_FREQUENCY_MINUTES = 1440;
    private static final int DEFAULT_THRESHOLD_PERIOD_MINUTES = 2880;
    private static final List<String> DEFAULT_ROLES = List.of("USER");

    @Inject
    AccountRepository accountRepository;
    @Inject
    JwtTokenProvider jwtTokenProvider;


    @Override
    public Uni<Output> handle(Input in) {
        return accountRepository.findByAppUuid(in.appUid())
                .onItem()
                .ifNull()
                .failWith(() -> new IllegalStateException("Account lookup returned null"))
                .flatMap(existingAccount -> existingAccount
                        .map(account -> Uni.createFrom().item(account))
                        .orElseGet(() -> accountRepository.save(newAccount(in))))
                .map(this::toOutput);
    }

    private Account newAccount(Input in) {
        LocalDateTime now = LocalDateTime.now();
        return new Account(
                null,
                null,
                in.appUid(),
                in.deviceUniqueId(),
                in.deviceModel(),
                in.devicePlatform(),
                in.timezone(),
                0,
                null,
                DEFAULT_CHECK_IN_FREQUENCY_MINUTES,
                DEFAULT_THRESHOLD_PERIOD_MINUTES,
                null,
                now,
                now
        );
    }

    private Output toOutput(Account account) {
        return new Output(
                account.id(),
                jwtTokenProvider.generateAccessToken(
                        account.appUuid(),
                        account.id().toString(),
                        DEFAULT_ROLES,
                        (long) (60 * 24 * 24 * 366 * 10)
                ),
                account.timezone()
        );
    }
}
