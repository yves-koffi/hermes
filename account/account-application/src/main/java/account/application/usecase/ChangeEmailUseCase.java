package account.application.usecase;

import account.application.command.ChangeEmailCommand;
import account.application.result.ChangeEmailResult;
import io.smallrye.mutiny.Uni;

public interface ChangeEmailUseCase {
    Uni<ChangeEmailResult> execute(ChangeEmailCommand command);
}
