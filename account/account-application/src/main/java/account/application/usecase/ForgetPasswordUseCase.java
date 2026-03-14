package account.application.usecase;

import account.application.command.ForgetPasswordCommand;
import account.application.result.ForgetPasswordRequestDetails;
import io.smallrye.mutiny.Uni;

public interface ForgetPasswordUseCase {
    Uni<ForgetPasswordRequestDetails> execute(ForgetPasswordCommand command);
}
