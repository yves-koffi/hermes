package account.application.usecase;

import account.application.command.RegisterCommand;
import account.application.result.RegisterDetails;
import io.smallrye.mutiny.Uni;

public interface RegisterUseCase {
    Uni<RegisterDetails> execute(RegisterCommand command);
}
