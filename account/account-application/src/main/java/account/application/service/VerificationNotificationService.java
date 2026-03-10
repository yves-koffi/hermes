package account.application.service;

import account.application.command.SendVerifyCodeCommand;
import account.application.result.VerifyCodeSentResult;
import account.application.usecase.VerificationNotificationUseCase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VerificationNotificationService implements VerificationNotificationUseCase {

    @Override
    public Uni<VerifyCodeSentResult> execute(SendVerifyCodeCommand command) {
        return Uni.createFrom().nullItem();
    }
}
