package account.application.service;

import account.application.command.LoginCommand;
import account.application.result.AuthDetails;
import account.application.spi.AccountRepository;
import account.application.usecase.LoginUseCase;
import account.domain.model.Account;
import account.domain.model.Provider;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import shared.application.spi.JwtTokenProvider;
import shared.domain.exception.AuthenticationException;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class LoginService implements LoginUseCase {

    @Inject
    AccountRepository accountRepository;
    @Inject
    JwtTokenProvider jwtTokenProvider;

    @Override
    public Uni<AuthDetails> execute(LoginCommand command) {
        return accountRepository.findByEmail(command.email())
                .flatMap(accountOpt -> {
                    if (accountOpt.isEmpty()) {
                        return Uni.createFrom().failure(AuthenticationException.invalidCredentials());
                    }

                    var account = accountOpt.get();
                    if (!isBasicPasswordValid(account, command.password())) {
                        return Uni.createFrom().failure(AuthenticationException.invalidCredentials());
                    }

                    if (!account.isActivated()) {
                        return Uni.createFrom().failure(AuthenticationException.accountUnverified(account.email()));
                    }

                    return Uni.createFrom().item(toAuthResult(account));
                });
    }

    private boolean isBasicPasswordValid(Account account, String password) {
        if (account.provider() != Provider.BASIC) {
            return false;
        }
        return account.password() != null && BcryptUtil.matches(password, account.password());
    }

    private AuthDetails toAuthResult(Account account) {
        UUID accountId = account.id();
        String accessToken = jwtTokenProvider.generateAccessToken(
                account.email(),
                accountId,
                List.of("USER"),
                null
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                UUID.randomUUID().toString(),
                accountId,
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
