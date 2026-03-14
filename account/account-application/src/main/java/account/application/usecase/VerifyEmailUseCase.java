package account.application.usecase;

import account.application.command.VerifyAccountCommand;
import account.application.result.AccountVerificationDetails;
import io.smallrye.mutiny.Uni;

public interface VerifyEmailUseCase {
    Uni<AccountVerificationDetails> execute(VerifyAccountCommand command);
}
