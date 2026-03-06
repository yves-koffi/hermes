package account.application.service;

import account.application.command.LoginCommand;
import account.application.mapper.AccountResultMapper;
import account.application.usecase.LoginUseCase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import shared.domain.model.TokenPair;

@ApplicationScoped
public class LoginService implements LoginUseCase {

   // AccountRepository accountRepository;

    @Inject
    AccountResultMapper accountResultMapper;


    @Override
    public Uni<TokenPair> execute(LoginCommand cmd) {
        return Uni.createFrom().item(null);
    }
}
