package account.application.usecase;

import io.smallrye.mutiny.Uni;

public interface ForgetPasswordUseCase {
    Uni<Void> execute();
}
