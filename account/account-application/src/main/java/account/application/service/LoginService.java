package account.application.service;

import account.application.command.LoginCommand;
import account.application.mapper.AccountResultMapper;
import account.application.result.AccountSummary;
import account.application.spi.AccountRepository;
import account.application.usecase.LoginUseCase;
import account.domain.model.Account;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@ApplicationScoped
public class LoginService implements LoginUseCase {

    @Inject
    AccountRepository accountRepository;

    @Inject
    AccountResultMapper accountResultMapper;

    @Override
    public Uni<AccountSummary> execute(LoginCommand cmd) {

        return Uni.createFrom().item(null);
    }

}
