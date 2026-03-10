package account.application.usecase;

import account.application.command.SendVerifyCodeCommand;
import account.application.result.VerifyCodeSentResult;
import io.smallrye.mutiny.Uni;

public interface VerificationNotificationUseCase {
    Uni<VerifyCodeSentResult> execute(SendVerifyCodeCommand command);
}
