package account.application.usecase;

import account.application.command.CheckTokenCommand;
import account.application.result.CheckTokenDetails;
import io.smallrye.mutiny.Uni;

public interface CheckTokenUseCase {
    Uni<CheckTokenDetails> execute(CheckTokenCommand command);
}
