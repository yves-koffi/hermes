package account.application.usecase;

import io.smallrye.mutiny.Uni;

public interface ResetPasswordUseCase {
    Uni<Void> execute();
}
