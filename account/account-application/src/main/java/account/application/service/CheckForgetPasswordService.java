package account.application.service;

import account.application.command.CheckForgetPasswordCommand;
import account.application.result.CheckForgetPasswordResult;
import account.application.usecase.CheckForgetPasswordUseCase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CheckForgetPasswordService implements CheckForgetPasswordUseCase {

    @Override
    public Uni<CheckForgetPasswordResult> execute(CheckForgetPasswordCommand command) {
        return Uni.createFrom().nullItem();
    }
}
