package account.application.service;

import account.application.command.ResetPasswordCommand;
import account.application.result.PasswordResetResult;
import account.application.usecase.ResetPasswordUseCase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ResetPasswordService implements ResetPasswordUseCase {

    @Override
    public Uni<PasswordResetResult> execute(ResetPasswordCommand command) {
        return Uni.createFrom().nullItem();
    }
}
