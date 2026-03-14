package account.application.service;

import account.application.command.SocialCredentialCommand;
import account.application.result.AuthDetails;
import account.application.spi.AccountRepository;
import account.application.usecase.SocialAuthUseCase;
import account.domain.model.Account;
import account.domain.model.Provider;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import shared.application.spi.JwtTokenProvider;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SocialAuthService implements SocialAuthUseCase {

    @Inject
    AccountRepository accountRepository;
    @Inject
    JwtTokenProvider jwtTokenProvider;

    @Override
    public Uni<AuthDetails> execute(SocialCredentialCommand command) {
        return accountRepository.findByEmail(command.email())
                .flatMap(accountOpt -> {
                    if (accountOpt.isPresent()) {
                        return Uni.createFrom().item(toAuthResult(accountOpt.get()));
                    }

                    Account account = new Account(
                            UUID.randomUUID(),
                            command.displayName(),
                            command.email(),
                            null,
                            null,
                            command.photoUrl(),
                            command.providerId(),
                            command.provider() == null ? Provider.GOOGLE : command.provider(),
                            OffsetDateTime.now(),
                            OffsetDateTime.now(),
                            OffsetDateTime.now()
                    );

                    return accountRepository.save(account)
                            .map(this::toAuthResult);
                });
    }

    private AuthDetails toAuthResult(Account account) {
        String accessToken = jwtTokenProvider.generateAccessToken(
                account.email(),
                account.id(),
                List.of("USER"),
                null
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                UUID.randomUUID().toString(),
                account.id(),
                null
        );

        return new AuthDetails(
                accessToken,
                refreshToken,
                "Bearer",
                0L,
                0L
        );
    }
}
