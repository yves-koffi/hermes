package account.application.usecase;

import account.application.command.CheckVerificationNotificationCodeCommand;
import account.application.result.CheckVerificationNotificationCodeResult;
import io.smallrye.mutiny.Uni;

public interface CheckVerificationNotificationCodeUseCase {
    Uni<CheckVerificationNotificationCodeResult> execute(CheckVerificationNotificationCodeCommand command);
}
