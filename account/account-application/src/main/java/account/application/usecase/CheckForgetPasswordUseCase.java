package account.application.usecase;

import account.application.command.CheckForgetPasswordCommand;
import account.application.result.CheckForgetPasswordResult;
import io.smallrye.mutiny.Uni;

public interface CheckForgetPasswordUseCase {
    Uni<CheckForgetPasswordResult> execute(CheckForgetPasswordCommand command);
}
