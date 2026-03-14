package account.application.usecase;

import account.application.command.ResetPasswordCommand;
import account.application.result.PasswordResetDetails;
import io.smallrye.mutiny.Uni;

public interface ResetPasswordUseCase {
    Uni<PasswordResetDetails> execute(ResetPasswordCommand command);
}
