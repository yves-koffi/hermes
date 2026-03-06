package account.application.usecase;

import account.application.command.LoginCommand;
import account.application.result.AccountSummary;
import io.smallrye.mutiny.Uni;
import shared.domain.model.TokenPair;


public interface LoginUseCase {
    Uni<TokenPair> execute(LoginCommand cmd);
}
