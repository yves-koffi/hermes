package account.application.usecase;

import account.application.command.ChangePasswordCommand;
import io.smallrye.mutiny.Uni;

public interface ChangePasswordUseCase {
    Uni<Void> execute(ChangePasswordCommand cmd);
}
