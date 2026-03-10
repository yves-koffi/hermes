package account.application.usecase;

import account.application.command.ForgetPasswordCommand;
import account.application.result.ForgetPasswordRequestResult;
import io.smallrye.mutiny.Uni;

public interface ForgetPasswordUseCase {
    Uni<ForgetPasswordRequestResult> execute(ForgetPasswordCommand command);
}
