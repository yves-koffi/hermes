package account.application.usecase;

import account.application.command.UpdateAccountCommand;
import io.smallrye.mutiny.Uni;

public interface UpdateAccountUseCase {
    Uni<Void> execute(UpdateAccountCommand command);
}
