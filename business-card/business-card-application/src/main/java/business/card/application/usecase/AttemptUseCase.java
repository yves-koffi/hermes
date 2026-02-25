package business.card.application.usecase;

import business.card.application.command.LoginCommand;
import business.card.application.result.AccountSummary;
import io.smallrye.mutiny.Uni;


public interface AttemptUseCase {
    Uni<AccountSummary> execute(LoginCommand cmd);
}
