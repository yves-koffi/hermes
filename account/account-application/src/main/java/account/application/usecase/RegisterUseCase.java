package account.application.usecase;

import account.application.command.RegisterCommand;
import account.application.result.RegisterResult;
import io.smallrye.mutiny.Uni;

public interface RegisterUseCase {
    Uni<RegisterResult> execute(RegisterCommand command);
}
