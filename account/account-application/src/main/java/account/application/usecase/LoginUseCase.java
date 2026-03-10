package account.application.usecase;

import account.application.command.LoginCommand;
import account.application.result.AuthResult;
import io.smallrye.mutiny.Uni;


public interface LoginUseCase {
    Uni<AuthResult> execute(LoginCommand command);
}
