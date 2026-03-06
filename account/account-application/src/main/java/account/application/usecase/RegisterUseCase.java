package account.application.usecase;

import account.application.command.CreateAccountCommand;
import account.application.result.AccountDetails;
import io.smallrye.mutiny.Uni;

public interface RegisterUseCase {
    Uni<AccountDetails> execute(CreateAccountCommand cmd);
}
