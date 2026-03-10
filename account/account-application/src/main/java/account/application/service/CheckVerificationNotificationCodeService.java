package account.application.service;

import account.application.command.CheckVerificationNotificationCodeCommand;
import account.application.result.CheckVerificationNotificationCodeResult;
import account.application.usecase.CheckVerificationNotificationCodeUseCase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CheckVerificationNotificationCodeService implements CheckVerificationNotificationCodeUseCase {

    @Override
    public Uni<CheckVerificationNotificationCodeResult> execute(CheckVerificationNotificationCodeCommand command) {
        return Uni.createFrom().nullItem();
    }
}
