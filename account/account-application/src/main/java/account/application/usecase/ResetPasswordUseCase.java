package account.application.usecase;

import account.application.command.ResetPasswordCommand;
import account.application.result.PasswordResetResult;
import io.smallrye.mutiny.Uni;

public interface ResetPasswordUseCase {
    Uni<PasswordResetResult> execute(ResetPasswordCommand command);
}
