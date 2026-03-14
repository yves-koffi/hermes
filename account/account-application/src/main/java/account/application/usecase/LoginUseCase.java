package account.application.usecase;

import account.application.command.LoginCommand;
import account.application.result.AuthDetails;
import io.smallrye.mutiny.Uni;


public interface LoginUseCase {
    Uni<AuthDetails> execute(LoginCommand command);
}
