package business.card.application.service;

import business.card.application.command.LoginCommand;
import business.card.application.mapper.AccountResultMapper;
import business.card.application.result.AccountSummary;
import business.card.application.spi.AccountRepository;
import business.card.application.usecase.AttemptUseCase;
import business.card.domain.model.Account;
import business.card.domain.model.Provider;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@ApplicationScoped
public class AttemptService implements AttemptUseCase {

    @Inject
    AccountRepository accountRepository;
    @Inject
    AccountResultMapper accountResultMapper;

    @Override
    public Uni<AccountSummary> execute(LoginCommand cmd) {
        if (cmd == null || isBlank(cmd.email()) || isBlank(cmd.provider_id())) {
            return Uni.createFrom().failure(new IllegalArgumentException("email and provider_id are required"));
        }

        return accountRepository.findByEmail(cmd.email().trim().toLowerCase())
                .flatMap(existing -> existing
                        .map(account -> Uni.createFrom().item(accountResultMapper.toSummary(account)))
                        .orElseGet(() -> accountRepository.save(buildNewAccount(cmd)).map(accountResultMapper::toSummary)));
    }

    private Account buildNewAccount(LoginCommand cmd) {
        Provider provider = toProvider(cmd.provider_id());
        String externalId = trimToNull(cmd.id());

        return new Account(
                null,
                trimToNull(cmd.displayName()),
                cmd.email().trim().toLowerCase(),
                trimToNull(cmd.photoUrl()),
                provider == Provider.GOOGLE ? externalId : null,
                provider == Provider.APPLE ? externalId : null,
                OffsetDateTime.now(ZoneOffset.UTC),
                provider,
                null,
                null
        );
    }

    private Provider toProvider(String providerId) {
        if (providerId == null) {
            throw new IllegalArgumentException("provider_id is required");
        }
        return switch (providerId.trim().toLowerCase()) {
            case "google" -> Provider.GOOGLE;
            case "apple" -> Provider.APPLE;
            default -> throw new IllegalArgumentException("unsupported provider_id: " + providerId);
        };
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
