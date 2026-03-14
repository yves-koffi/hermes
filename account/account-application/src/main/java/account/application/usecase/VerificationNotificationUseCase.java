package account.application.usecase;

import account.application.command.SendVerifyCodeCommand;
import account.application.result.VerifyCodeSentDetails;
import io.smallrye.mutiny.Uni;

public interface VerificationNotificationUseCase {
    Uni<VerifyCodeSentDetails> execute(SendVerifyCodeCommand command);
}
