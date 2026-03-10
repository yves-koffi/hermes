package account.application.usecase;

import account.application.command.VerifyAccountCommand;
import account.application.result.AccountVerificationResult;
import io.smallrye.mutiny.Uni;

public interface VerifyEmailUseCase {
    Uni<AccountVerificationResult> execute(VerifyAccountCommand command);
}
