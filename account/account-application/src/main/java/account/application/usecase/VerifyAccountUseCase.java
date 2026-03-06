package account.application.usecase;

import io.smallrye.mutiny.Uni;

public interface VerifyAccountUseCase {
    Uni<Void> execute();
}
